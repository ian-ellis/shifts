package com.github.ianellis.shifts.networking

import com.github.ianellis.shifts.entities.ShiftEntity
import com.github.ianellis.shifts.entities.ShiftsResponseEntity
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object ShiftResponseConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>,
                                       retrofit: Retrofit): Converter<ResponseBody, *>? {

        return if (type == ShiftsResponseEntity::class.java) {
            ShiftsResponseConverter
        } else {
            null
        }
    }

    private object ShiftsResponseConverter : Converter<ResponseBody, ShiftsResponseEntity> {

        private val parser: JsonParser = JsonParser()

        override fun convert(value: ResponseBody): ShiftsResponseEntity {
            val body = value.string()
            return if (body == "null") {
                ShiftsResponseEntity(emptyList())
            } else {
                val listOfShifts = parser.parse(body).asJsonArray.map {
                    val jsonObj: JsonObject = it.asJsonObject
                    ShiftEntity(
                        id = jsonObj.get("id").asInt,
                        start = jsonObj.get("start").asString,
                        end = jsonObj.get("end").asString,
                        startLatitude = jsonObj.get("startLatitude").asString,
                        startLongitude = jsonObj.get("startLongitude").asString,
                        endLatitude = jsonObj.get("endLatitude").asString,
                        endLongitude = jsonObj.get("endLongitude").asString,
                        image = jsonObj.get("image").asString
                    )
                }
                ShiftsResponseEntity(listOfShifts)
            }
        }
    }
}