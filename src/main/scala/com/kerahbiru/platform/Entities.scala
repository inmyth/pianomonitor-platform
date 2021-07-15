package com.kerahbiru.platform

import facade.amazonaws.services.iot.{CertificateArn, CertificateId, CertificatePem, KeyPair, PolicyArn, PolicyName}
import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveUnwrappedDecoder

import java.util.UUID
//import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto._
import io.circe.generic.extras.defaults._
//import io.circe.generic.auto._
//import io.circe.parser.decode

object Entities {

  // topic : topic/userid/client111
  /*
  {
  [
    {
    "type": "temperature",
    "value": 30.1
    },
    {
    "type": "humidity",
    "value": 13
    }
  ]
  }


   */
  case class UserId(value: String) extends AnyVal

  case class ClientId(value: String) extends AnyVal

  object ClientId {

    def generate: ClientId = ClientId(UUID.randomUUID().toString)
  }

//  case class CertificateId(value: String) extends AnyVal

//  case class CertificateArn(value: String) extends AnyVal

//  case class PolicyId(value: String) extends AnyVal

//  case class PolicyName(value: String) extends AnyVal

//  case class Topic(value: String) extends AnyVal

  object Topic {

    def fromClientId(clientId: ClientId) = {}

  }

  case class CertCreationResponse(certificateArn: CertificateArn, certificatePem: CertificatePem, keyPair: KeyPair)

//  case class AwsResponse(
//      statusCode: Int,
//      body: String
//  )

//  case class CreateKeyAndCertificateResponse(
//                                              certificateArn: CertificateArn,
//                                              certificateId: CertificateId,
//                                              certificatePem: String,
//                                              keyPair: KeyPair
//  )

//  case class KeyPair(PrivateKey: String, PublicKey: String)

  // userId clientId policyArn certificateArn certificateId policyArn policyId
  //   "body": "{\"certificateArn\":\"arn:aws:iot:us-west-2:734435107319:cert/41d198566129e802bb036f446e5910c712f95df3a53ed950c5dfba2b739258cd\",\"certificateId\":\"41d198566129e802bb036f446e5910c712f95df3a53ed950c5dfba2b739258cd\",\"certificatePem\":\"-----BEGIN CERTIFICATE-----\\nMIIDWTCCAkGgAwIBAgIUDkcFrqsjv90gbLLetUSm2PVX/x0wDQYJKoZIhvcNAQEL\\nBQAwTTFLMEkGA1UECwxCQW1hem9uIFdlYiBTZXJ2aWNlcyBPPUFtYXpvbi5jb20g\\nSW5jLiBMPVNlYXR0bGUgU1Q9V2FzaGluZ3RvbiBDPVVTMB4XDTIxMDcxMzE1MzIz\\nMloXDTQ5MTIzMTIzNTk1OVowHjEcMBoGA1UEAwwTQVdTIElvVCBDZXJ0aWZpY2F0\\nZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANh3DQz8gT5SqLfBlNKg\\n6fAR2PeeS9UU55hx7ynhi3o4LLZPJ/1aK69ReTKvOOlObUqyherF8+DWA58qkO/J\\nwBOlHHyjL4shMM1BJz6fGl8QAAzKK/uM2q/Vjf0+Tf4Qpo1dUV+ItNFtZFIr39Vc\\nUKEWcGLaS0oLUB4kcm4q8Iyfwaq4fG5q7LhFQnA24GCZe9jSSy8YbZnZiFdJjjXD\\nbXa0Bo6/j6HOeyl3ZTflcBY/l9Fc+QyiyOz6SAMvC09RQUi9Z4wQx0ARnuyEsVgg\\nxmUrawZ0nFOm3GUJHttdUbs9k5mE632BDHuqt346WXaEG+V4xl3rXodNewYJOC3z\\njz8CAwEAAaNgMF4wHwYDVR0jBBgwFoAUnGEkc/u2sVuwoYuyImZ5MDqX8+gwHQYD\\nVR0OBBYEFAduxQ9VKAeFjCCOXsfOKIWqESnaMAwGA1UdEwEB/wQCMAAwDgYDVR0P\\nAQH/BAQDAgeAMA0GCSqGSIb3DQEBCwUAA4IBAQCgYMTVe4pPgKLgVY63vpD+0/0T\\ng4ODZJOTSc1Ci0B9GHv44xqSvajvfVMX4lIauYcXRwQKwXWtNxsbUjtHhn6XKr5/\\nUQ5znV0140wwlTEZsWAEMY1GoJtBLWc265Ba9xX6VXxeaaAAxM2aq18HKybW0qI3\\nLJ0H8PL59Crnykc0+79Q/8DhmT9lc8TsgXQzvImrZjAeIMWp01tSnKgDSjnfAwE8\\nD2+oxg8wCr8MMgYbnm10AZwRM9jMkHli+NxmsAIpNm1wXnsaCYpzjpL0RANa2vaE\\npGFllzDj6LK5wx1iukp0YY/t1+XcaYgB7cgui9xoHeBCaMUNso8NEQ940USy\\n-----END CERTIFICATE-----\\n\",\"keyPair\":{\"PublicKey\":\"-----BEGIN PUBLIC KEY-----\\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2HcNDPyBPlKot8GU0qDp\\n8BHY955L1RTnmHHvKeGLejgstk8n/Vorr1F5Mq846U5tSrKF6sXz4NYDnyqQ78nA\\nE6UcfKMviyEwzUEnPp8aXxAADMor+4zar9WN/T5N/hCmjV1RX4i00W1kUivf1VxQ\\noRZwYtpLSgtQHiRybirwjJ/Bqrh8bmrsuEVCcDbgYJl72NJLLxhtmdmIV0mONcNt\\ndrQGjr+Poc57KXdlN+VwFj+X0Vz5DKLI7PpIAy8LT1FBSL1njBDHQBGe7ISxWCDG\\nZStrBnScU6bcZQke211Ruz2TmYTrfYEMe6q3fjpZdoQb5XjGXeteh017Bgk4LfOP\\nPwIDAQAB\\n-----END PUBLIC KEY-----\\n\",\"PrivateKey\":\"-----BEGIN RSA PRIVATE KEY-----\\nMIIEowIBAAKCAQEA2HcNDPyBPlKot8GU0qDp8BHY955L1RTnmHHvKeGLejgstk8n\\n/Vorr1F5Mq846U5tSrKF6sXz4NYDnyqQ78nAE6UcfKMviyEwzUEnPp8aXxAADMor\\n+4zar9WN/T5N/hCmjV1RX4i00W1kUivf1VxQoRZwYtpLSgtQHiRybirwjJ/Bqrh8\\nbmrsuEVCcDbgYJl72NJLLxhtmdmIV0mONcNtdrQGjr+Poc57KXdlN+VwFj+X0Vz5\\nDKLI7PpIAy8LT1FBSL1njBDHQBGe7ISxWCDGZStrBnScU6bcZQke211Ruz2TmYTr\\nfYEMe6q3fjpZdoQb5XjGXeteh017Bgk4LfOPPwIDAQABAoIBAAUW/4qPoWZawTbY\\n6WvudrG7abOg2D2haTSvcSU19XahPYPn5pPRe/d6XfQwJJFv06gO/JEhqHNf9OPY\\nN2I8BzUvC7VujN6joCcjYg3Z5d/YW3fxhQk8LaQ2zEjpHyJbN99ZE2HAsCfAf7xi\\n5bd5nTe8dFYOtfh+vZqH1tz6S3+y8uMzdQgVTCWenKRrOCmIKMJBVcQyCc4a7WlE\\nNt29+BNkPhfGAnr+sxfLwGsP2BRH3+tokhUr2v1JGI+em35GCyOwVa24sMVJthRv\\nTWkmdEbzhq91KOfJhDn8NgOsG49xz4CbfCqvOTldGlAIxMGSGYuzNxgZXBaKp8VM\\n8hC7KLkCgYEA8EIcPDjJh9wNyroa91c4rSv8hjz5mU/uVxE8ECCnmztgKPOeihg0\\n5fyCGJWHXfvfEikFtFQlhD1hLMhHhrVto/5OD/eeBcQw5U7mMsC7NRNbsAYSZq3C\\n8R2ZuGt2rU1ASUUl2vcjeJRSPfHmQiWEUCOcc8yuE38tdU1U167uNWsCgYEA5qXa\\ngmdp3qTv0ujM52qsrbae8bYucesa83FOwgkCRKHwxi33nTWtK8T9POIdwa1n9uiM\\n2HMPGaLKzbJSJu0HbdK+2vnjzkd31qnZVCdzSPzJpOjRyS02ezoSiS1DolgqUbxe\\nRX2pgzGMsS11BNHv1BjIwP0lVwJacmAKXo5u7n0CgYEAwDgUQPYp/qFHA4fL5VGK\\nDbpgacHE7CzwAgbO6BRIJEDn2Tu3/sIJKGvSZfrT95R4zx2066Za8abt5RdDanoj\\nAw6Fw+6XJgPmHr/2GmrqHDelpnnlJPMmQSQuhUatB1AD+9rvw3TwkxsPhXHQh+Fe\\nL4OeO2Npbj3Uz8GT/I9/E4sCgYAZijoFAKCHh+oKh77QzU4rujIlf8/qy2bjmCNp\\ndwfqQ6heuY5WuS/gzeJg0IUNAj+l5qBaA3ujkhQ07M8OAmc1MQXzR4KDQAS0H4Cy\\nQeARL/TALwuz5C7JPGf7CqkXm/2rqVkjBsQzg9QZdngjvX96E/h+7kkpNlh/m8Tq\\nLYl5HQKBgG6gJC3QFRReiTVk5mfYZaXEHVvzhW79ZDCkJcek654KaYDGl+yYqQTf\\nxj/pex7wJhtRTNt2JstR/OrVBnRSW938jL96Q/KDfKlyGxf/ya9UkySKxiV2XswL\\nc27DIHsg4gADDOdO3bNJ+7Lmyq++JWy04ZAhlR0/tufd1pGviRFC\\n-----END RSA PRIVATE KEY-----\\n\"}}"
//  implicit val AwsResponseDecoder: Decoder[AwsResponse] = deriveDecoder
//
//  implicit val CreateKeyAndCertificateResponseDecoder: Decoder[CreateKeyAndCertificateResponse] = deriveDecoder
//
//  implicit val CertificateArnDecoder: Decoder[CertificateArn] = deriveUnwrappedDecoder
//
//  implicit val KeyPairDecoder: Decoder[KeyPair] = deriveDecoder

  /*
  implicit val currencyEncoder: Encoder[Currency] =
    Encoder.instance[Currency] { show.show _ andThen Json.fromString }

  implicit val pairEncoder: Encoder[Pair] =
    deriveConfiguredEncoder[Pair]

  implicit val rateEncoder: Encoder[Rate] =
    deriveConfiguredEncoder[Rate]

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveConfiguredEncoder[GetApiResponse]
   */
}
