package izumi.test


package object domain02 {
type RTestEnumIndirect = izumi.test.domain01.RTestEnum

type RTestEnumDirect = izumi.test.domain01.RTestEnum

type ImportedBasicFailure = izumi.test.domain01.BasicFailure

type ImportedIDForDomain2 = izumi.test.domain01.IDForDomain2

type AliasedTestObject = izumi.test.domain01.TestObject

type AliasedGoAliasEnumTest = izumi.test.domain01.GoAliasEnumTest

type RTestObject1 = izumi.test.domain01.RTestObject1

type AnyValTest2 = izumi.test.domain01.AnyValTest2

type UserId = String
}
           
       