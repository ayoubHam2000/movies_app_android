package com.example.movieapp.Services

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.movieapp.Classes.Utils.VolleyFileUploadRequest
import com.example.movieapp.Modules.FileDataPart
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


object JsonTask {

    //#############################################
    //#############################################
    //#############################################

    fun getJsonObject(context : Context, url : String, complete : (Boolean, JSONObject) -> Unit){
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            {response ->
                try{
                    Log.d(">>>JSON REQUEST STATUE", "Getting JSON Array SUCCESS")
                    complete(true, response)
                }catch (e : JSONException){
                    Log.d(">>>JSON REQUEST STATUE", "ERROR ${e.localizedMessage}")
                    complete(false, JSONObject())
                }
            },
            { error ->
                Log.d(">>>JSON REQUEST STATUE", "ERROR $error")
                complete(false, JSONObject())
            }
        )
        Volley.newRequestQueue(context).add(jsonObjectRequest)
    }

    fun getJsonArray(context : Context, url : String, complete : (Boolean, JSONArray) -> Unit){
        val jsonArrayRequest  = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    Log.d(">>>JSON REQUEST STATUE", "Getting JSON Array SUCCESS")
                    complete(true, response)
                } catch (e: JSONException) {
                    Log.d(">>>JSON REQUEST STATUE", "ERROR ${e.localizedMessage}")
                    complete(false, JSONArray())
                }
            },
            { error ->
                Log.d(">>>JSON REQUEST STATUE", "ERROR $error")
                complete(false, JSONArray())
            }
        )
        Volley.newRequestQueue(context).add(jsonArrayRequest)
    }

    //#############################################
    //#############################################
    //#############################################
    //Response.ErrorListener

    fun postJsonObjectFile(context : Context, url : String, jsonObj : JSONObject, imageData : ByteArray?, complete : (Boolean, String) -> Unit) {

        val request = object : VolleyFileUploadRequest(
            Request.Method.POST,
            url,
            {response ->
                try{
                    Log.d(">>>JSON REQUEST STATUE", "Success : ${response.data}")
                    complete(true, response.toString())
                }catch (e : VolleyError){
                    Log.d(">>>JSON REQUEST STATUE", "Error : $e")
                    complete(false, "")
                }
            },
            {error ->
                Log.d(">>>JSON REQUEST STATUE", "Error : $error")
                complete(false, "")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val p = mutableMapOf<String, String>()
                for(s in jsonObj.keys()){
                    p[s] = jsonObj.getString(s)
                }
                return p
            }
            override fun getByteData(): MutableMap<String, FileDataPart> {
                val params = HashMap<String, FileDataPart>()
                params["file"] = FileDataPart("image.jpg", imageData!!, "jpeg")
                return params
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            20000,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun postJsonObject(context : Context, url : String, jsonObj : JSONObject, complete : (Boolean, String) -> Unit) {

        val request = object : VolleyFileUploadRequest(
            Request.Method.POST,
            url,
            {response ->
                try{
                    Log.d(">>>JSON REQUEST STATUE", "Success : ${response.data}")
                    complete(true, response.toString())
                }catch (e : VolleyError){
                    Log.d(">>>JSON REQUEST STATUE", "Error : $e")
                    complete(false, "")
                }
            },
            {error ->
                Log.d(">>>JSON REQUEST STATUE", "Error : $error")
                complete(false, "")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val p = mutableMapOf<String, String>()
                for(s in jsonObj.keys()){
                    p[s] = jsonObj.getString(s)
                }
                return p
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            20000,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }




}