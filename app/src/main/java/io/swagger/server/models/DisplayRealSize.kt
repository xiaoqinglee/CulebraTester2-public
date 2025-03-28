/**
* CulebraTester
* ## Snaky Android Test --- If you want to be able to try out the API using the **Execute** or **TRY** button from this page - an android device should be connected using `adb` - the server should have been started using `./culebratester2 start-server`  then you will be able to invoke the API and see the responses. 
*
* OpenAPI spec version: 2.0.74
* 
*
* NOTE: This class is auto generated by the swagger code generator program.
* https://github.com/swagger-api/swagger-codegen.git
* Do not edit the class manually.
*/package io.swagger.server.models


/**
 * 
 * @param device 
 * @param x 
 * @param y 
 * @param artWidth 
 * @param artHeight 
 * @param screenshotWidth 
 * @param screenshotX 
 * @param screenshotY 
 */
data class DisplayRealSize (

    val device: kotlin.String,
    val x: kotlin.Int,
    val y: kotlin.Int,
    val artWidth: kotlin.Int? = null,
    val artHeight: kotlin.Int? = null,
    val screenshotWidth: kotlin.Int? = null,
    val screenshotX: kotlin.Int? = null,
    val screenshotY: kotlin.Int? = null
) {
}
