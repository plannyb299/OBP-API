package code.api.berlin.group.v1_3

import code.api.BerlinGroup.ScaStatus
import code.api.Constant
import code.api.Constant.SYSTEM_READ_TRANSACTIONS_BERLIN_GROUP_VIEW_ID
import code.api.berlin.group.v1_3.JSONFactory_BERLIN_GROUP_1_3.{CancellationJsonV13, InitiatePaymentResponseJson, StartPaymentAuthorisationJson}
import code.api.berlin.group.v1_3.model.{PsuData, ScaStatusResponse, UpdatePsuAuthenticationResponse}
import code.api.builder.PaymentInitiationServicePISApi.APIMethods_PaymentInitiationServicePISApi
import code.api.util.APIUtil.OAuth._
import code.api.util.APIUtil.extractErrorMessageCode
import code.api.util.ErrorMessages.{AuthorisationNotFound, InvalidJsonFormat, NotPositiveAmount, _}
import code.model.dataAccess.{BankAccountRouting, MappedBankAccount}
import code.setup.{APIResponse, DefaultUsers}
import code.transactionrequests.TransactionRequests.{PaymentServiceTypes, TransactionRequestTypes}
import code.views.Views
import com.github.dwickern.macros.NameOf.nameOf
import com.openbankproject.commons.model.enums.AccountRoutingScheme
import com.openbankproject.commons.model.{ErrorMessage, SepaCreditTransfers, SepaCreditTransfersBerlinGroupV13, ViewId}
import net.liftweb.json.Serialization.write
import net.liftweb.mapper.By
import org.scalatest.Tag

import scala.collection.immutable.List

class PaymentInitiationServicePISApiTest extends BerlinGroupServerSetupV1_3 with DefaultUsers {

  object PIS extends Tag("Payment Initiation Service (PIS)")
  object initiatePayment extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.initiatePayments))
  object getPaymentInformation extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.getPaymentInformation))
  object getPaymentInitiationStatus extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.getPaymentInitiationStatus))
  
  object startPaymentAuthorisationTransactionAuthorisation extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.startPaymentAuthorisationTransactionAuthorisation ))
  object startPaymentAuthorisationUpdatePsuAuthentication extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.startPaymentAuthorisationUpdatePsuAuthentication))
  object startPaymentAuthorisationSelectPsuAuthenticationMethod extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.startPaymentAuthorisationSelectPsuAuthenticationMethod))
  object getPaymentInitiationAuthorisation extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.getPaymentInitiationAuthorisation))
  object getPaymentInitiationScaStatus extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.getPaymentInitiationScaStatus))
  object updatePaymentPsuDataTransactionAuthorisation extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.updatePaymentPsuDataTransactionAuthorisation))
  object updatePaymentPsuDataUpdatePsuAuthentication extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.updatePaymentPsuDataUpdatePsuAuthentication))
  object updatePaymentPsuDataSelectPsuAuthenticationMethod extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.updatePaymentPsuDataSelectPsuAuthenticationMethod))
  object updatePaymentPsuDataAuthorisationConfirmation extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.updatePaymentPsuDataAuthorisationConfirmation))

  
  object cancelPayment extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.cancelPayment))
  object startPaymentInitiationCancellationAuthorisationTransactionAuthorisation extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.startPaymentInitiationCancellationAuthorisationTransactionAuthorisation))
  object startPaymentInitiationCancellationAuthorisationUpdatePsuAuthentication extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.startPaymentInitiationCancellationAuthorisationUpdatePsuAuthentication))
  object startPaymentInitiationCancellationAuthorisationSelectPsuAuthenticationMethod extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.startPaymentInitiationCancellationAuthorisationSelectPsuAuthenticationMethod))
  object getPaymentInitiationCancellationAuthorisationInformation extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.getPaymentInitiationCancellationAuthorisationInformation))
  object getPaymentCancellationScaStatus extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.getPaymentCancellationScaStatus))
  object updatePaymentCancellationPsuDataTransactionAuthorisation extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.updatePaymentCancellationPsuDataTransactionAuthorisation))
  object updatePaymentCancellationPsuDataUpdatePsuAuthentication extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.updatePaymentCancellationPsuDataUpdatePsuAuthentication))
  object updatePaymentCancellationPsuDataSelectPsuAuthenticationMethod extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.updatePaymentCancellationPsuDataSelectPsuAuthenticationMethod))
  object updatePaymentCancellationPsuDataAuthorisationConfirmation extends Tag(nameOf(APIMethods_PaymentInitiationServicePISApi.updatePaymentCancellationPsuDataAuthorisationConfirmation))

  feature(s"test the BG v1.3 -${initiatePayment.name}") {
    scenario("Failed Case - Wrong Json format Body", BerlinGroupV1_3, PIS, initiatePayment) {
      val wrongInitiatePaymentJson =
        s"""{
           |"instructedAmount1": {
           |  "currency": "EUR",
           |  "amount": "1234"
           |},
           |"creditorAccount": {
           |  "iban": "DE75512108001245126199"
           |},
           |"creditorName": "70charname"
            }""".stripMargin

      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString).POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, wrongInitiatePaymentJson)
      Then("We should get a 400 ")
      response.code should equal(400)
      val error = s"$InvalidJsonFormat The Json body should be the $SepaCreditTransfersBerlinGroupV13 "
      And("error should be " + error)
      response.body.extract[ErrorMessage].message should startWith (error)
    }
    scenario("Failed Case - wrong amount", BerlinGroupV1_3, PIS, initiatePayment) {
      val wrongAmountInitiatePaymentJson =
        s"""{
           | "debtorAccount": {
           |   "iban": "123"
           | },
           |"instructedAmount": {
           |  "currency": "EUR",
           |  "amount": "-1234"
           |},
           |"creditorAccount": {
           |  "iban": "DE75512108001245126199"
           |},
           |"creditorName": "70charname"
            }""".stripMargin

      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString).POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, wrongAmountInitiatePaymentJson)
      Then("We should get a 400 ")
      response.code should equal(400)
      val error = s"${NotPositiveAmount} Current input is: '-1234'"
      And("error should be " + error)
      response.body.extract[ErrorMessage].message contains extractErrorMessageCode(NotPositiveAmount) should be (true)
    }
    scenario("Successful case - small amount -- change the balance", BerlinGroupV1_3, PIS, initiatePayment) {
      val accountsRoutingIban = BankAccountRouting.findAll(By(BankAccountRouting.AccountRoutingScheme, AccountRoutingScheme.IBAN.toString))
      val acountRoutingIbanFrom = accountsRoutingIban.head
      val acountRoutingIbanTo = accountsRoutingIban.last

      val beforePaymentFromAccountBalance = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanFrom.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanFrom.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")
      val beforePaymentToAccountBalance = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanTo.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanTo.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")

      val initiatePaymentJson =
        s"""{
           | "debtorAccount": {
           |   "iban": "${acountRoutingIbanFrom.accountRouting.address}"
           | },
           |"instructedAmount": {
           |  "currency": "EUR",
           |  "amount": "12"
           |},
           |"creditorAccount": {
           |  "iban": "${acountRoutingIbanTo.accountRouting.address}"
           |},
           |"creditorName": "70charname"
            }""".stripMargin
      
      grantAccountAccess(acountRoutingIbanFrom)

      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString).POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, initiatePaymentJson)
      Then("We should get a 201 ")
      response.code should equal(201)
      val payment = response.body.extract[InitiatePaymentResponseJson]
      payment.transactionStatus should be ("ACCP")
      payment.paymentId should not be null
      payment._links.scaStatus should not be null


      val afterPaymentFromAccountBalance = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanFrom.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanFrom.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")
      val afterPaymentToAccountBalacne = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanTo.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanTo.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")

      afterPaymentFromAccountBalance-beforePaymentFromAccountBalance should be (BigDecimal(-12))
      afterPaymentToAccountBalacne-beforePaymentToAccountBalance should be (BigDecimal(12))
    }
    scenario("Successful case - big amount -- do not change the balance", BerlinGroupV1_3, PIS, initiatePayment) {
      val accountsRoutingIban = BankAccountRouting.findAll(By(BankAccountRouting.AccountRoutingScheme, AccountRoutingScheme.IBAN.toString))
      val acountRoutingIbanFrom = accountsRoutingIban.head
      val acountRoutingIbanTo = accountsRoutingIban.last

      val beforePaymentFromAccountBalance = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanFrom.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanFrom.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")
      val beforePaymentToAccountBalance = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanTo.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanTo.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")

      val initiatePaymentJson =
        s"""{
           | "debtorAccount": {
           |   "iban": "${acountRoutingIbanFrom.accountRouting.address}"
           | },
           |"instructedAmount": {
           |  "currency": "EUR",
           |  "amount": "2001"
           |},
           |"creditorAccount": {
           |  "iban": "${acountRoutingIbanTo.accountRouting.address}"
           |},
           |"creditorName": "70charname"
            }""".stripMargin

      grantAccountAccess(acountRoutingIbanFrom)

      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString).POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, initiatePaymentJson)
      Then("We should get a 201 ")
      response.code should equal(201)
      val payment = response.body.extract[InitiatePaymentResponseJson]
      payment.transactionStatus should be ("RCVD")
      payment.paymentId should not be null
      payment._links.scaStatus should not be null

      val afterPaymentFromAccountBalance = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanFrom.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanFrom.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")
      val afterPaymentToAccountBalacne = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanTo.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanTo.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")

      afterPaymentFromAccountBalance-beforePaymentFromAccountBalance should be (BigDecimal(0))
      afterPaymentToAccountBalacne-beforePaymentToAccountBalance should be (BigDecimal(0))
    }
  }

  private def grantAccountAccess(acountRoutingIbanFrom: BankAccountRouting) = {
    Views.views.vend.systemView(ViewId(SYSTEM_READ_TRANSACTIONS_BERLIN_GROUP_VIEW_ID)).flatMap(view =>
      // Grant account access
      Views.views.vend.grantAccessToSystemView(acountRoutingIbanFrom.bankId,
        acountRoutingIbanFrom.accountId,
        view,
        resourceUser1
      )
    )
  }

  feature(s"test the BG v1.3 -${getPaymentInformation.name}") {
    scenario("Successful case ", BerlinGroupV1_3, PIS, initiatePayment) {
      val accountsRoutingIban = BankAccountRouting.findAll(By(BankAccountRouting.AccountRoutingScheme, AccountRoutingScheme.IBAN.toString))
      val ibanFrom = accountsRoutingIban.head.accountRouting.address
      val ibanTo = accountsRoutingIban.last.accountRouting.address

      val initiatePaymentJson =
        s"""{
           | "debtorAccount": {
           |   "iban": "${ibanFrom}"
           | },
           |"instructedAmount": {
           |  "currency": "EUR",
           |  "amount": "123"
           |},
           |"creditorAccount": {
           |  "iban": "${ibanTo}"
           |},
           |"creditorName": "70charname"
            }""".stripMargin

      grantAccountAccess(accountsRoutingIban.head)

      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString).POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, initiatePaymentJson)
      Then("We should get a 201 ")
      response.code should equal(201)
      val payment = response.body.extract[InitiatePaymentResponseJson]
      payment.transactionStatus should be ("ACCP")
      payment.paymentId should not be null

      Then(s"we test the ${getPaymentInformation.name}")
      val paymentId = payment.paymentId
      val requestGet = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId).GET <@ (user1)
      val responseGet: APIResponse = makeGetRequest(requestGet)
      responseGet.code should be (200)
      responseGet.body.extract[SepaCreditTransfers].instructedAmount.currency should be ("EUR")
      responseGet.body.extract[SepaCreditTransfers].instructedAmount.amount should be ("123")
      responseGet.body.extract[SepaCreditTransfers].debtorAccount.iban should be (ibanFrom)
      responseGet.body.extract[SepaCreditTransfers].creditorAccount.iban should be (ibanTo)


    }
  }
  feature(s"test the BG v1.3 -${getPaymentInitiationStatus.name}") {
    scenario("Successful case ", BerlinGroupV1_3, PIS, initiatePayment) {
      val accountsRoutingIban = BankAccountRouting.findAll(By(BankAccountRouting.AccountRoutingScheme, AccountRoutingScheme.IBAN.toString))
      val ibanFrom = accountsRoutingIban.head.accountRouting.address
      val ibanTo = accountsRoutingIban.last.accountRouting.address

      val initiatePaymentJson =
        s"""{
           | "debtorAccount": {
           |   "iban": "${ibanFrom}"
           | },
           |"instructedAmount": {
           |  "currency": "EUR",
           |  "amount": "2001"
           |},
           |"creditorAccount": {
           |  "iban": "${ibanTo}"
           |},
           |"creditorName": "70charname"
            }""".stripMargin

      grantAccountAccess(accountsRoutingIban.head)

      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString).POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, initiatePaymentJson)
      Then("We should get a 201 ")
      response.code should equal(201)
      val payment = response.body.extract[InitiatePaymentResponseJson]
      payment.transactionStatus should be ("RCVD")
      payment.paymentId should not be null
      payment._links.scaStatus should not be null

      Then(s"we test the ${getPaymentInitiationStatus.name}")
      val paymentId = payment.paymentId
      val requestGet = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId / "status").GET <@ (user1)
      val responseGet: APIResponse = makeGetRequest(requestGet)
      responseGet.code should be (200)
      (responseGet.body \ "transactionStatus").extract[String] should be ("RCVD")
      (responseGet.body \ "fundsAvailable").extract[Boolean] should be (true)
    }
  }
  feature(s"test the BG v1.3 ${startPaymentAuthorisationTransactionAuthorisation.name} and ${getPaymentInitiationAuthorisation.name} and ${getPaymentInitiationScaStatus.name} and ${updatePaymentPsuDataTransactionAuthorisation.name}") {
    scenario(s"${startPaymentAuthorisationTransactionAuthorisation.name} Failed Case - Wrong PaymentId", BerlinGroupV1_3, PIS, startPaymentAuthorisationTransactionAuthorisation) {
     
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "authorisations").POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, """{"scaAuthenticationData":"123"}""".stripMargin)
      Then("We should get a 400 ")
      response.code should equal(400)
      response.body.extract[ErrorMessage].message should startWith (InvalidTransactionRequestId)
    }
    scenario(s"Successful Case ", BerlinGroupV1_3, PIS, startPaymentAuthorisationTransactionAuthorisation) {
      val accountsRoutingIban = BankAccountRouting.findAll(By(BankAccountRouting.AccountRoutingScheme, AccountRoutingScheme.IBAN.toString)).filterNot(_.bankId.value == "DEFAULT_BANK_ID_NOT_SET")
      val acountRoutingIbanFrom = accountsRoutingIban.head
      val acountRoutingIbanTo = accountsRoutingIban.last

      val beforePaymentFromAccountBalance = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanFrom.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanFrom.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")
      val beforePaymentToAccountBalance = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanTo.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanTo.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")

      
      val initiatePaymentJson =
        s"""{
           | "debtorAccount": {
           |   "iban": "${acountRoutingIbanFrom.accountRouting.address}"
           | },
           |"instructedAmount": {
           |  "currency": "EUR",
           |  "amount": "2001"
           |},
           |"creditorAccount": {
           |  "iban": "${acountRoutingIbanTo.accountRouting.address}"
           |},
           |"creditorName": "70charname"
            }""".stripMargin

      grantAccountAccess(accountsRoutingIban.head)

      val requestInitiatePaymentJson = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString).POST <@ (user1)
      val responseInitiatePaymentJson: APIResponse = makePostRequest(requestInitiatePaymentJson, initiatePaymentJson)
      Then("We should get a 201 ")
      responseInitiatePaymentJson.code should equal(201)
      val paymentResponseInitiatePaymentJson = responseInitiatePaymentJson.body.extract[InitiatePaymentResponseJson]
      paymentResponseInitiatePaymentJson.transactionStatus should be ("RCVD")
      paymentResponseInitiatePaymentJson.paymentId should not be null

      val paymentId = paymentResponseInitiatePaymentJson.paymentId

      Then(s"we test the ${startPaymentAuthorisationTransactionAuthorisation.name}")
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId / "authorisations").POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, """{"scaAuthenticationData":"123"}""".stripMargin)
      Then("We should get a 201 ")
      response.code should equal(201)
      val startPaymentAuthorisationResponse = response.body.extract[StartPaymentAuthorisationJson]
      startPaymentAuthorisationResponse.authorisationId should not be null
      startPaymentAuthorisationResponse.psuMessage should be ("Please check your SMS at a mobile device.")
      startPaymentAuthorisationResponse.scaStatus should be (ScaStatus.received.toString)
      startPaymentAuthorisationResponse._links.scaStatus should not be null
      
      Then(s"We can test the ${getPaymentInitiationAuthorisation.name}")
      val requestGetPaymentInitiationAuthorisation = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId / "authorisations").GET <@ (user1)
      val responseGetPaymentInitiationAuthorisation: APIResponse = makeGetRequest(requestGetPaymentInitiationAuthorisation)
      responseGetPaymentInitiationAuthorisation.code should be (200)
      responseGetPaymentInitiationAuthorisation.body.extract[List[StartPaymentAuthorisationJson]].length > 0 should be (true)
      val paymentInitiationAuthorisation = responseGetPaymentInitiationAuthorisation.body.extract[List[StartPaymentAuthorisationJson]].head
      val authorisationId = paymentInitiationAuthorisation.authorisationId
      paymentInitiationAuthorisation.scaStatus should be (ScaStatus.received.toString)

      Then(s"We can test the ${getPaymentInitiationScaStatus.name}")
      val requestGetPaymentInitiationScaStatus = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId / "authorisations" /authorisationId).GET <@ (user1)
      val responseGetPaymentInitiationScaStatus: APIResponse = makeGetRequest(requestGetPaymentInitiationScaStatus)
      responseGetPaymentInitiationScaStatus.code should be (200)
      val paymentInitiationScaStatus = (responseGetPaymentInitiationScaStatus.body \ "scaStatus").extract[String]
      paymentInitiationScaStatus should be (ScaStatus.received.toString)

      Then(s"We can test the ${updatePaymentPsuDataTransactionAuthorisation.name}")
      val updatePaymentPsuDataJsonBody = APIMethods_PaymentInitiationServicePISApi
        .resourceDocs
        .filter( _.partialFunction == APIMethods_PaymentInitiationServicePISApi.updatePaymentPsuDataTransactionAuthorisation)
        .head.exampleRequestBody.asInstanceOf[JvalueCaseClass] //All the Json String convert to JvalueCaseClass implicitly 
        .jvalueToCaseclass
      
      val requestUpdatePaymentPsuData = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId / "authorisations"/authorisationId).PUT <@ (user1)
      val responseUpdatePaymentPsuData: APIResponse = makePutRequest(requestUpdatePaymentPsuData, write(updatePaymentPsuDataJsonBody))
      responseUpdatePaymentPsuData.code should be (200)
      responseUpdatePaymentPsuData.body.extract[ScaStatusResponse].scaStatus should be("finalised")

      Thread.sleep(100) // wait for 100 milliseconds
      
      val afterPaymentFromAccountBalance = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanFrom.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanFrom.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")
      val afterPaymentToAccountBalacne = MappedBankAccount.find(
        By(MappedBankAccount.bank, acountRoutingIbanTo.bankId.value),
        By(MappedBankAccount.theAccountId, acountRoutingIbanTo.accountId.value))
        .map(_.balance).openOrThrowException("Can not be empty here")

      afterPaymentFromAccountBalance-beforePaymentFromAccountBalance should be (BigDecimal(-2001.00))
      afterPaymentToAccountBalacne-beforePaymentToAccountBalance should be (BigDecimal(2001.00))
      
    }
    
  }
  
  feature(s"test the BG v1.3 ${updatePaymentPsuDataUpdatePsuAuthentication} and ${updatePaymentPsuDataUpdatePsuAuthentication.name}") {
    scenario(s"${startPaymentAuthorisationTransactionAuthorisation.name}" , BerlinGroupV1_3, PIS, updatePaymentPsuDataUpdatePsuAuthentication) {
     
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "authorisations" / "AUTHORISATION_ID").PUT <@ (user1)
      val response: APIResponse = makePutRequest(requestPost, """{"psuData": {"password": "start12"}}""".stripMargin)
      Then("We should get a 200 ")
      response.code should equal(200)
    }
  }
  
  feature(s"test the BG v1.3 ${updatePaymentPsuDataSelectPsuAuthenticationMethod} and ${updatePaymentPsuDataSelectPsuAuthenticationMethod.name}") {
    scenario(s"${startPaymentAuthorisationTransactionAuthorisation.name}" , BerlinGroupV1_3, PIS, updatePaymentPsuDataSelectPsuAuthenticationMethod) {
      val requestPut = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "authorisations" / "AUTHORISATION_ID").PUT <@ (user1)
      val response: APIResponse = makePutRequest(requestPut, """{"authenticationMethodId":""}""".stripMargin)
      Then("We should get a 200 ")
      response.code should equal(200)
    }
  }
  
  feature(s"test the BG v1.3 ${updatePaymentPsuDataAuthorisationConfirmation} and ${updatePaymentPsuDataAuthorisationConfirmation.name}") {
    scenario(s"${startPaymentAuthorisationTransactionAuthorisation.name}" , BerlinGroupV1_3, PIS, updatePaymentPsuDataAuthorisationConfirmation) {
     
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "authorisations"/"AUTHORISATION_ID").PUT <@ (user1)
      val response: APIResponse = makePutRequest(requestPost, """{"confirmationCode":"confirmationCode"}""".stripMargin)
      Then("We should get a 200 ")
      response.code should equal(200)
    }
  }
  
  
  feature(s"test the BG v1.3 ${startPaymentAuthorisationUpdatePsuAuthentication.name}") {
    scenario(s"${startPaymentAuthorisationUpdatePsuAuthentication.name} ", BerlinGroupV1_3, PIS, startPaymentAuthorisationUpdatePsuAuthentication) {
     
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "authorisations").POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, """{  "psuData":{"password":"start12"  }}""".stripMargin)
      Then("We should get a 201")
      response.code should equal(201)
    }
  }
  feature(s"test the BG v1.3 ${startPaymentAuthorisationSelectPsuAuthenticationMethod.name}") {
    scenario(s"${startPaymentAuthorisationSelectPsuAuthenticationMethod.name} ", BerlinGroupV1_3, PIS, startPaymentAuthorisationSelectPsuAuthenticationMethod) {
     
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "authorisations").POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, """{"authenticationMethodId":""}""".stripMargin)
      Then("We should get a 201")
      response.code should equal(201)
    }
  }
  
  feature(s"test the BG v1.3 ${startPaymentInitiationCancellationAuthorisationTransactionAuthorisation.name} " +
    s"and ${getPaymentInitiationCancellationAuthorisationInformation.name} " +
    s"and ${getPaymentCancellationScaStatus.name}" +
    s"and ${updatePaymentCancellationPsuDataTransactionAuthorisation.name}") {
    scenario(s"${startPaymentInitiationCancellationAuthorisationTransactionAuthorisation.name} Failed Case - Wrong PaymentId", BerlinGroupV1_3, PIS, startPaymentInitiationCancellationAuthorisationTransactionAuthorisation) {

      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "cancellation-authorisations").POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, """{"scaAuthenticationData":""}""")
      Then("We should get a 400 ")
      response.code should equal(400)
      response.body.extract[ErrorMessage].message should startWith (InvalidTransactionRequestId)
    }
    scenario(s"Successful Case ", BerlinGroupV1_3, PIS) {

      val accountsRoutingIban = BankAccountRouting.findAll(By(BankAccountRouting.AccountRoutingScheme, AccountRoutingScheme.IBAN.toString))
      val ibanFrom = accountsRoutingIban.head.accountRouting.address
      val ibanTo = accountsRoutingIban.last.accountRouting.address

      val initiatePaymentJson =
        s"""{
           | "debtorAccount": {
           |   "iban": "${ibanFrom}"
           | },
           |"instructedAmount": {
           |  "currency": "EUR",
           |  "amount": "123"
           |},
           |"creditorAccount": {
           |  "iban": "${ibanTo}"
           |},
           |"creditorName": "70charname"
            }""".stripMargin

      grantAccountAccess(accountsRoutingIban.head)

      val requestInitiatePaymentJson = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString).POST <@ (user1)
      val responseInitiatePaymentJson: APIResponse = makePostRequest(requestInitiatePaymentJson, initiatePaymentJson)
      Then("We should get a 201 ")
      responseInitiatePaymentJson.code should equal(201)
      val paymentResponseInitiatePaymentJson = responseInitiatePaymentJson.body.extract[InitiatePaymentResponseJson]
      paymentResponseInitiatePaymentJson.transactionStatus should be ("ACCP")

      val paymentId = paymentResponseInitiatePaymentJson.paymentId

      Then(s"we test the ${cancelPayment.name}")
      val requestDelete = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId).DELETE <@ (user1)
      val responseDelete: APIResponse = makeDeleteRequest(requestDelete)
      Then("We should get a 202")
      responseDelete.code should equal(202)
      
      Then(s"we test the ${startPaymentInitiationCancellationAuthorisationTransactionAuthorisation.name}")
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId / "cancellation-authorisations").POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, """{"scaAuthenticationData":""}""")
      Then("We should get a 201 ")
      response.code should equal(201)
      val startPaymentAuthorisationResponse = response.body.extract[UpdatePsuAuthenticationResponse]
      startPaymentAuthorisationResponse.authorisationId should not be null
      startPaymentAuthorisationResponse.psuMessage should be (Some("Please check your SMS at a mobile device."))
      startPaymentAuthorisationResponse.scaStatus should be (ScaStatus.received.toString)

      Then(s"We can test the ${getPaymentInitiationCancellationAuthorisationInformation.name}")
      val requestGetPaymentInitiationCancellationAuthorisationInformation = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId / "cancellation-authorisations").GET <@ (user1)
      val responseGetPaymentInitiationCancellationAuthorisationInformation: APIResponse = makeGetRequest(requestGetPaymentInitiationCancellationAuthorisationInformation)
      responseGetPaymentInitiationCancellationAuthorisationInformation.code should be (200)
      responseGetPaymentInitiationCancellationAuthorisationInformation.body.extract[CancellationJsonV13].cancellationIds.length > 0 should be (true)
      val cancellationJsonV13 = responseGetPaymentInitiationCancellationAuthorisationInformation.body.extract[CancellationJsonV13].cancellationIds
      val cancelationId = cancellationJsonV13.head

      Then(s"We can test the ${getPaymentCancellationScaStatus.name}")
      val requestGetPaymentCancellationScaStatus = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId / "cancellation-authorisations" /cancelationId).GET <@ (user1)
      val responseGetPaymentCancellationScaStatus: APIResponse = makeGetRequest(requestGetPaymentCancellationScaStatus)
      responseGetPaymentCancellationScaStatus.code should be (200)
      val cancellationScaStatus = (responseGetPaymentCancellationScaStatus.body \ "scaStatus").extract[String]
      cancellationScaStatus should be (ScaStatus.received.toString)

      Then(s"We can test the ${updatePaymentCancellationPsuDataTransactionAuthorisation.name}")

      val requestUpdatePaymentCancellationPsuData = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / paymentId / "cancellation-authorisations"/cancelationId).PUT <@ (user1)
      val responseUpdatePaymentCancellationPsuData: APIResponse = makePutRequest(requestUpdatePaymentCancellationPsuData, """{"scaAuthenticationData":"123"}""")
      responseUpdatePaymentCancellationPsuData.code should be (200)
      responseUpdatePaymentCancellationPsuData.body.extract[ScaStatusResponse].scaStatus should be("finalised")

    }
  }

  feature(s"test the BG v1.3 ${updatePaymentCancellationPsuDataUpdatePsuAuthentication.name}" ) {
    scenario(s"${updatePaymentCancellationPsuDataUpdatePsuAuthentication.name}", BerlinGroupV1_3, PIS, updatePaymentCancellationPsuDataUpdatePsuAuthentication) {

      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "cancellation-authorisations" /"authorisationId").PUT <@ (user1)
      val response: APIResponse = makePutRequest(requestPost, """{"psuData":{"password":"start12"}}""")
      Then("We should get a 200 ")
      response.code should equal(200)
    }
  }

  feature(s"test the BG v1.3 ${updatePaymentCancellationPsuDataSelectPsuAuthenticationMethod.name}" ) {
    scenario(s"${updatePaymentCancellationPsuDataSelectPsuAuthenticationMethod.name}", BerlinGroupV1_3, PIS, updatePaymentCancellationPsuDataSelectPsuAuthenticationMethod) {
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "cancellation-authorisations"/"authorisationId").PUT <@ (user1)
      val response: APIResponse = makePutRequest(requestPost, """{"authenticationMethodId":""}""")
      Then("We should get a 200 ")
      response.code should equal(200)
    }
  }

  feature(s"test the BG v1.3 ${updatePaymentCancellationPsuDataAuthorisationConfirmation.name}" ) {
    scenario(s"${updatePaymentCancellationPsuDataAuthorisationConfirmation.name}", BerlinGroupV1_3, PIS, updatePaymentCancellationPsuDataAuthorisationConfirmation) {
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "cancellation-authorisations"/"authorisationId").PUT <@ (user1)
      val response: APIResponse = makePutRequest(requestPost, """{"confirmationCode":"confirmationCode"}""")
      Then("We should get a 200 ")
      response.code should equal(200)
    }
  }

  feature(s"test the BG v1.3 ${startPaymentInitiationCancellationAuthorisationUpdatePsuAuthentication.name}") {
    scenario(s"${startPaymentInitiationCancellationAuthorisationUpdatePsuAuthentication.name}", BerlinGroupV1_3, PIS, startPaymentInitiationCancellationAuthorisationUpdatePsuAuthentication) {
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "cancellation-authorisations").POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, """{"psuData":{"password":"start12"}}""")
      Then("We should get a 201 ")
      response.code should equal(201)
    }
  }

  feature(s"test the BG v1.3 ${startPaymentInitiationCancellationAuthorisationSelectPsuAuthenticationMethod.name}") {
    scenario(s"${startPaymentInitiationCancellationAuthorisationSelectPsuAuthenticationMethod.name}", BerlinGroupV1_3, PIS, startPaymentInitiationCancellationAuthorisationSelectPsuAuthenticationMethod) {
      val requestPost = (V1_3_BG / PaymentServiceTypes.payments.toString / TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString / "PAYMENT_ID" / "cancellation-authorisations").POST <@ (user1)
      val response: APIResponse = makePostRequest(requestPost, """{"authenticationMethodId":""}""")
      Then("We should get a 201 ")
      response.code should equal(201)
    }
  }

  feature("test the BG v1.3 getPaymentCancellationScaStatus") {
    scenario("Successful call endpoint getPaymentCancellationScaStatus", BerlinGroupV1_3, PIS, getPaymentCancellationScaStatus) {
      When("Post empty to call initiatePayment")
      val cancellationId = "NON_EXISTING_CANCELLATION_ID"
      val requestGet = (V1_3_BG /
        PaymentServiceTypes.bulk_payments.toString /
        TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString /
        "PAYMENT_ID" /
        "cancellation-authorisations" /
        cancellationId).POST <@ (user1)
      val response: APIResponse = makeGetRequest(requestGet)
      Then("We should get a 400 ")
      response.code should equal(400)
      val error = s"$InvalidTransactionRequestId Current TransactionRequestId(PAYMENT_ID) "
      And("error should be " + error)
      response.body.extract[ErrorMessage].message should equal (error)
    }
  }
  feature("test the BG v1.3 getPaymentInitiationAuthorisation") {
    scenario("Successful call endpoint getPaymentInitiationAuthorisation", BerlinGroupV1_3, PIS, getPaymentInitiationAuthorisation) {
      When("Post empty to call initiatePayment")
      val requestGet = (V1_3_BG /
        PaymentServiceTypes.bulk_payments.toString /
        TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString /
        "NON_EXISTING_PAYMENT_ID" /
        "authorisations").POST <@ (user1)
      val response: APIResponse = makeGetRequest(requestGet)
      Then("We should get a 400 ")
      response.code should equal(400)
      val error = s"$InvalidTransactionRequestId Current TransactionRequestId(NON_EXISTING_PAYMENT_ID) "
      And("error should be " + error)
      response.body.extract[ErrorMessage].message should equal (error)
    }
  }
  feature("test the BG v1.3 getPaymentInitiationCancellationAuthorisationInformation") {
    scenario("Successful call endpoint getPaymentInitiationCancellationAuthorisationInformation", BerlinGroupV1_3, PIS, getPaymentInitiationCancellationAuthorisationInformation) {
      When("Post empty to call initiatePayment")
      val requestGet = (V1_3_BG /
        PaymentServiceTypes.bulk_payments.toString /
        TransactionRequestTypes.SEPA_CREDIT_TRANSFERS.toString /
        "NON_EXISTING_PAYMENT_ID" /
        "cancellation-authorisations").POST <@ (user1)
      val response: APIResponse = makeGetRequest(requestGet)
      Then("We should get a 200 ")
      response.code should equal(200)
      val payment = response.body.extract[CancellationJsonV13]
      payment.cancellationIds should be equals(0)
    }
  }

}