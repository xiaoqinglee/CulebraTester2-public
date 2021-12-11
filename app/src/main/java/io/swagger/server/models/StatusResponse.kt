/**
* CulebraTester
* ## Snaky Android Test --- If you want to be able to try out the API using the **Execute** or **TRY** button from this page - an android device should be connected using `adb` - the server should have been started using `./culebratester2 start-server`  then you will be able to invoke the API and see the responses. 
*
* OpenAPI spec version: 2.0.17
* 
*
* NOTE: This class is auto generated by the swagger code generator program.
* https://github.com/swagger-api/swagger-codegen.git
* Do not edit the class manually.
*/package io.swagger.server.models


/**
 *  * @param status  * @param statusCode  * @param errorMessage */
data class StatusResponse (        val status: StatusResponse.Status,    val statusCode: kotlin.Int? = null,    val errorMessage: kotlin.String? = null
) {
    /**
    * 
    * Values: OK,ERROR,UNKNOWN
    */
    enum class Status(val value: kotlin.String){
        OK("OK"),
        ERROR("ERROR"),
        UNKNOWN("UNKNOWN");
    }
}
