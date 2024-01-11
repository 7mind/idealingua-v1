package izumi.idealingua.runtime.rpc

abstract class IRTTransportException(message: String, cause: Option[Throwable]) extends RuntimeException(message, cause.orNull)

class IRTUnparseableDataException(message: String, cause: Option[Throwable] = None) extends IRTTransportException(message, cause)

class IRTDecodingException(message: String, cause: Option[Throwable] = None) extends IRTTransportException(message, cause)

class IRTTypeMismatchException(message: String, val v: Any, cause: Option[Throwable] = None) extends IRTTransportException(message, cause)

class IRTMissingHandlerException(message: String, val v: Any, cause: Option[Throwable] = None) extends IRTTransportException(message, cause)

class IRTLimitReachedException(message: String, cause: Option[Throwable] = None) extends IRTTransportException(message, cause)

class IRTUnathorizedRequestContextException(message: String, cause: Option[Throwable] = None) extends IRTTransportException(message, cause)

class IRTGenericFailure(message: String, cause: Option[Throwable] = None) extends IRTTransportException(message, cause)
