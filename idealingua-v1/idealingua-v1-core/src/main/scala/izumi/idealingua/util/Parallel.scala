package izumi.idealingua.util

import java.util.concurrent.{ForkJoinPool, ForkJoinTask, RecursiveTask}

import scala.collection.immutable.ArraySeq
import scala.reflect.ClassTag

/** Order-preserving parallel-map helper.
  *
  * Backed by a ForkJoinPool and `ForkJoinTask.fork()/join()` so nested fan-outs
  * (e.g. per-member rendering inside per-domain rendering) compose without
  * pool starvation: when a worker blocks on `join()`, the FJ runtime steals
  * pending tasks instead of idling.
  *
  * Constructed once per CLI invocation and threaded through the compiler
  * pipeline. Test/tool call sites that don't want to manage a pool use
  * [[Parallel.Default]], which delegates to [[ForkJoinPool.commonPool]].
  */
final class Parallel(val parallelism: Int, private val pool: ForkJoinPool) {

  def parMap[A, B: ClassTag](xs: Seq[A])(f: A => B): Seq[B] = {
    val n = xs.length
    if (n == 0) Seq.empty
    else if (n == 1 || parallelism <= 1) xs.map(f)
    else {
      // Build N fork-join tasks, fork them, then join in input order.
      val tasks = new Array[ForkJoinTask[B]](n)
      val it    = xs.iterator
      var i     = 0
      while (it.hasNext) {
        val a = it.next()
        tasks(i) = new RecursiveTask[B] {
          protected def compute(): B = f(a)
        }
        i += 1
      }

      // If we're already inside a ForkJoinPool worker thread (nested call),
      // fork directly — tasks join the host pool's work-stealing queue.
      // Otherwise, submit into our pool from outside the FJ runtime.
      val currentWorker = ForkJoinTask.getPool()
      if (currentWorker eq pool) {
        i = 0
        while (i < n) { tasks(i).fork(); i += 1 }
      } else {
        i = 0
        while (i < n) { pool.execute(tasks(i)); i += 1 }
      }

      val out = new Array[B](n)
      i = 0
      while (i < n) {
        // ForkJoinTask.join rethrows RuntimeException / Error directly; no
        // ExecutionException wrapping (unlike Future.get).
        out(i) = tasks(i).join()
        i += 1
      }
      ArraySeq.unsafeWrapArray(out)
    }
  }

  def parForeach[A](xs: Seq[A])(f: A => Unit): Unit = {
    val _ = parMap(xs)(f)
  }
}

object Parallel {

  /** Default instance backed by [[ForkJoinPool.commonPool]] — the JVM-managed
    * shared pool. Used by tests and one-shot tools that don't care to size
    * their own pool.
    */
  val Default: Parallel = {
    val pool = ForkJoinPool.commonPool()
    new Parallel(math.max(1, pool.getParallelism), pool)
  }

  def apply(parallelism: Int): Parallel = {
    val p = math.max(1, parallelism)
    new Parallel(p, new ForkJoinPool(p))
  }
}
