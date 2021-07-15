package com.kerahbiru.platform

import org.scalatest.flatspec.AnyFlatSpec
//import io.circe.generic.auto._

class EntitiesTest extends AnyFlatSpec {

  behavior of "Json Nested Parsing"

//  val x =
//    """
//      |{
//      |  "statusCode": 200,
//      |  "body": "{\"certificateArn\":\"arn:aws:iot:us-west-2:734435107319:cert/b67a816a5339277788e24553d531067b3c0cd09502f900a2cc71ccb98de2a9c7\",\"certificateId\":\"b67a816a5339277788e24553d531067b3c0cd09502f900a2cc71ccb98de2a9c7\",\"certificatePem\":\"-----BEGIN CERTIFICATE-----\\nMIIDWTCCAkGgAwIBAgIUFodku9mon4dkMBhvVW0anoi7IGMwDQYJKoZIhvcNAQEL\\nBQAwTTFLMEkGA1UECwxCQW1hem9uIFdlYiBTZXJ2aWNlcyBPPUFtYXpvbi5jb20g\\nSW5jLiBMPVNlYXR0bGUgU1Q9V2FzaGluZ3RvbiBDPVVTMB4XDTIxMDcxNDAzNDEx\\nM1oXDTQ5MTIzMTIzNTk1OVowHjEcMBoGA1UEAwwTQVdTIElvVCBDZXJ0aWZpY2F0\\nZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMd8erdl1eInocF8MSSJ\\ntKIoQEoajDxazjy+Hp3js4bALyraC2E9i5CBvGyznJdCBNYWlpEitbTC6T5/v6KM\\n9jcLm39STGwNSu6uX5mnbnSn1UlCBn7EASbwdceELCB0xrb95CNMCSgdtVX8RAcY\\npyfdtkiK7saUx/vFHwaTvVEUtxDTOnbsqbQxHTMYrhwAYEBgzZEc+zs8kqG5h5Cs\\nYZYHuEAGL1bFYc9YY2h8K6PAkohEjPJR8d8M9bgEJNk9IGCTty3skd2zyxnq4uuU\\nvdByysm1ABhV0iUyyTGykuU5tX+TVOTH+YO8CXBp2l6Kkcd7NNvh1saZGiESAWXY\\n6E8CAwEAAaNgMF4wHwYDVR0jBBgwFoAUdkxzbpoNlUCQzWjuGGM8ZHerbPYwHQYD\\nVR0OBBYEFL24RL5zFTCMfbXDAUBxI0S5HZcbMAwGA1UdEwEB/wQCMAAwDgYDVR0P\\nAQH/BAQDAgeAMA0GCSqGSIb3DQEBCwUAA4IBAQBl97BsWpqs24U2Wxh7QOQbwNBd\\nAP+Tr00cBg/o6KdcGc0vrgADDuI/9tngQ6378hDLEiXXYzt1E1Em0PbmOYIgTrU8\\n+xLfkFbFiwHIfi1r6QUFU4QLCkgbJm9uk8y95IdFqbIgO58GBZ5Z7UEEKsxz3RDf\\nO+cw2K1fSyjntcW5lQa8MXmsKcPhVkV7jM5ffzjg992A/ncIP168jB3YnLEBD6KD\\nFimrC9yKnwdrclALH+9msHld6Q0qwvn31Rd2OX1f9skDUW2+wABr8HK/OEGlFZTZ\\nuwAnF2znEprx4y5wQDEU2xKiSWOIcXYOKs9yBfgRF21gWMFkt1AyaZegk/t3\\n-----END CERTIFICATE-----\\n\",\"keyPair\":{\"PublicKey\":\"-----BEGIN PUBLIC KEY-----\\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx3x6t2XV4iehwXwxJIm0\\noihAShqMPFrOPL4eneOzhsAvKtoLYT2LkIG8bLOcl0IE1haWkSK1tMLpPn+/ooz2\\nNwubf1JMbA1K7q5fmadudKfVSUIGfsQBJvB1x4QsIHTGtv3kI0wJKB21VfxEBxin\\nJ922SIruxpTH+8UfBpO9URS3ENM6duyptDEdMxiuHABgQGDNkRz7OzySobmHkKxh\\nlge4QAYvVsVhz1hjaHwro8CSiESM8lHx3wz1uAQk2T0gYJO3LeyR3bPLGeri65S9\\n0HLKybUAGFXSJTLJMbKS5Tm1f5NU5Mf5g7wJcGnaXoqRx3s02+HWxpkaIRIBZdjo\\nTwIDAQAB\\n-----END PUBLIC KEY-----\\n\",\"PrivateKey\":\"-----BEGIN RSA PRIVATE KEY-----\\nMIIEpAIBAAKCAQEAx3x6t2XV4iehwXwxJIm0oihAShqMPFrOPL4eneOzhsAvKtoL\\nYT2LkIG8bLOcl0IE1haWkSK1tMLpPn+/ooz2Nwubf1JMbA1K7q5fmadudKfVSUIG\\nfsQBJvB1x4QsIHTGtv3kI0wJKB21VfxEBxinJ922SIruxpTH+8UfBpO9URS3ENM6\\nduyptDEdMxiuHABgQGDNkRz7OzySobmHkKxhlge4QAYvVsVhz1hjaHwro8CSiESM\\n8lHx3wz1uAQk2T0gYJO3LeyR3bPLGeri65S90HLKybUAGFXSJTLJMbKS5Tm1f5NU\\n5Mf5g7wJcGnaXoqRx3s02+HWxpkaIRIBZdjoTwIDAQABAoIBABtoMsP3rccAguy+\\nT1IhyTtuJEcKFrJEF4mo5TP0ELo2tURB6MZXWlFkQar9kkoFu/xd5hnSlvxauwjn\\nLED89ObjJV38xJsjyGO6eU/2/OnyFQ+XjqTj8PSUnNnogteCs7p4b1uaGrf0PPGv\\nUvlWncCQaXbV/7AwyLkCgBy7N4tn+u2tRgFBHaewhEObY2+g6TF+/K29JxXJkGlR\\nY/sTjA1jNfVHFl1a6Uhx/63SlftO5dm1bFhNeqgISW3I7jYalbHdKm1Nieirk2x1\\nToLv60548s42eZbaWkZ6QJuMEv8ZTdSNwJDFlOYn6eyxAiSPk4LheyIrIiBkhQcj\\n2yd85QECgYEA8rhRYC1IthDak5ukDXYSfWBJJ1QhyareC1XEP3nmZBlQWfoXzlo8\\nyy+k39RNe9ITya5M8wtnJdN3f8JJClrkUcIWrcGaqJKt+tFcT4RmYfdB1vz2/ts9\\npb1CSxq/kasrgsL8eQoHqqTEBneES3jeKNK3j1zLj2Euir7oFQq1V28CgYEA0maa\\nimEX3uqybi4KUdz2nq8vPig7Fbscu01FiT3Llmx5Z8/3X5vxXrA2S/inISa3yH41\\nwGgPOCgoz0pj/RDOEMurqB28RMfaXfyQ/ky6LfLFyPLN7qnRvoWbc1kefXC/7WyJ\\nhVmcGGcw33jexIgkDIoschoIZVHkxsc9/N14DSECgYEAr6FL0OozmPqtDu4qUUtE\\nqdBIXtrz7/dh4JXZAY11dq0ShWkAXOLS5ru88/Gtd5FVnDQxnhR8ONTBvYKaU5Hd\\ncIOe4XAcNyqcFcf0od1OMEk4LRayq0t9WjaOh8X9JKzVOTNaeUMWtRWwenV1Fx+I\\nscV36Zbd/Qv3H5JwYPl0qqUCgYEAv/hGAQRd11KkwsrXN6I4ksicfkYOwuEacfsq\\n+vGbFACdNQ8XUfDi5dY5dE/HjgPZo+EkAg3v9PkPYzMoz/nJSqHKWXXoFYmTwDV1\\nxdET/mCH/bUsGBpx+TfGghRKMYHyXBqvzy/XsJxWh0+tUfw1iAt6ShFen7N83bFk\\n6ZbYtoECgYAzEcc5UwP3Q6cxHpQzy/TFIg2FUte0GPQvORO4Gjv4c8HH6Yhf2J4C\\nAKHh0xI1QpWzLAW8uiBmdI/mncWM0EmsVnqgaSEhFwFa9icKOAyK3K6PhR7tbW1U\\nFATfLcTy49T7kRb2pzsUlzvJkKmNPFlxM/q+otu8T7V7vYikVIEBOA==\\n-----END RSA PRIVATE KEY-----\\n\"}}"
//      |}
//      |""".stripMargin
//
//  it should "Parse all componentes correctly" in {
//    val a = decode[AwsResponse](x).toOption.get
//
//    val b = decode[CreateKeyAndCertificateResponse](a.body).toOption.get
//    assert(
//      b.certificateArn.value === "arn:aws:iot:us-west-2:734435107319:cert/b67a816a5339277788e24553d531067b3c0cd09502f900a2cc71ccb98de2a9c7"
//    )
//  }
}
