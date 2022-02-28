package org.fhir.resources.collector.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.fhir.resources.collector.FhirResourcesCollectorProperties;
import org.fhir.resources.collector.model.Constants;
import org.fhir.resources.collector.model.RemoteResponse;
import org.fhir.resources.collector.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;

@Service
public class FhirResourcesCollectorService {

	@Autowired
	private RemoteServices remoteServices;

	@Autowired
	private FhirResourcesCollectorProperties fihrResourcesCollectorProperties;

	public Response getEnrichedJson(String inputJson)
			throws InterruptedException, ExecutionException, TimeoutException {
		Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
		DocumentContext jsonContext = JsonPath.parse(inputJson);
		JsonObject jsonObject = new JsonObject();
		JsonParser parser = new JsonParser();
		List<String> referencePathsInJson = new ArrayList<String>();
		Response response = new Response();
		/**
		 * take out all the paths in json, having reference word.
		 */
		try {
			referencePathsInJson = JsonPath.using(conf).parse(inputJson).read("$..reference");
		} catch (PathNotFoundException pathNotFoundException) {

		}

		JsonElement parseOrignalJson = parser.parse(inputJson);

		/**
		 * adding input json in jsonobject.
		 */
		jsonObject.add("orignaljson", parseOrignalJson);

		/**
		 * map to keep track of arrays present in json.
		 */
		Map<String, Integer> keyMap = new HashMap<>();
		CompletableFuture<JsonObject> jsonFuture = null;

		/**
		 * if no reference to remote service in input json, return input json
		 */
		if (referencePathsInJson != null) {
			List<CompletableFuture<RemoteResponse>> requestResponseFutures = referencePathsInJson.stream()
					.map(path -> remoteServices.getRemoteResponse(path, inputJson)).collect(Collectors.toList());
			CompletableFuture<Void> allFutures = CompletableFuture
					.allOf(requestResponseFutures.toArray(new CompletableFuture[requestResponseFutures.size()]));

			CompletableFuture<List<RemoteResponse>> allCompletableFuture = allFutures.thenApply(future -> {
				return requestResponseFutures.stream().map(completableFuture -> completableFuture.join())
						.collect(Collectors.toList());
			});

			jsonFuture = allCompletableFuture.thenApply(requestResponseList -> {
				/**
				 * iterate each response from remote service
				 */
				for (RemoteResponse requestResponse : requestResponseList) {
					/**
					 * check if any remote services gave exception
					 */
					if (requestResponse.getRemoteServiceFail() == true) {
						response.setIsExceptionOccured(true);
					}
					ArrayList<String> keyList = filterJsonKey(requestResponse.getPath(),
							jsonContext.read(requestResponse.getPath()));
					populateJsonWithRefrences(keyList, jsonObject, requestResponse.getResponse(), keyMap);
				}
				return jsonObject;
			});
		}

		String jsonWithRefrenceData = jsonFuture
				.get(fihrResourcesCollectorProperties.getGlobalTimeOut(), TimeUnit.SECONDS).toString();
		response.setJsonWithRefrenceData(jsonWithRefrenceData);
		return response;
	}

	/**
	 * populate json with remote response against keys in keylist, keyMap is used to
	 * check if that key is already there in json, if so, a sequence no is used to
	 * generate keys
	 */
	public JsonObject populateJsonWithRefrences(ArrayList<String> keyList, JsonObject jsonObject, String remoteResponse,
			Map<String, Integer> keyMap) {

		JsonParser parser = new JsonParser();
		JsonElement parsedRemoteResponse = parser.parse(remoteResponse);
		if (keyList.size() < 2) {
			keyList.stream().forEach(key -> {
				if (keyMap.containsKey(key)) {

					Integer value = keyMap.get(key);
					if (value == 1) {
						JsonElement json = jsonObject.get(key);
						jsonObject.remove(key);
						String newkey1 = key + Constants.UNDESCORE + value;
						jsonObject.add(newkey1, json);
					}
					value = value + 1;
					String newkey = key + Constants.UNDESCORE + value;
					jsonObject.add(newkey, parsedRemoteResponse);
					keyMap.put(key, value);

				} else {
					jsonObject.add(key, parsedRemoteResponse);
					keyMap.put(key, 1);
				}
			});
		} else {

			JsonObject childJson = new JsonObject();
			childJson.add(keyList.get(0), parsedRemoteResponse);
			if (keyMap.containsKey(keyList.get(1))) {

				Integer value = keyMap.get(keyList.get(1));
				if (value == 1) {
					JsonElement json = jsonObject.get(keyList.get(1));
					jsonObject.remove(keyList.get(1));
					String newkey1 = keyList.get(1) + Constants.UNDESCORE + value;
					jsonObject.add(newkey1, json);

				}
				value = value + 1;
				String newkey = keyList.get(1) + Constants.UNDESCORE + value;
				jsonObject.add(newkey, childJson);
				keyMap.put(keyList.get(1), value);
			} else {
				jsonObject.add(keyList.get(1), childJson);
				keyMap.put(keyList.get(1), 1);
			}

		}
		return jsonObject;
	}

	/**
	 * this function populates keys to be added in response json. if this is json
	 * "patient":{"reference":"Patient/636200","display":"Bo157 King743"} list will
	 * have {patient} as key.
	 * 
	 * if json is "diagnosisReference":{"reference":"Condition/636284"} list will
	 * have {condition,dignosisReference} as keys, condition being child and
	 * dignosisReference being parent.
	 */
	public ArrayList<String> filterJsonKey(String path, String resourceUri) {

		int index = resourceUri.indexOf("/");
		String firstWord = resourceUri.substring(0, index);
		String[] strarray = formatreferencepath(path);
		ArrayList<String> nodeList = new ArrayList<String>();
		Boolean notFound = true;
		int i = strarray.length - 2;
		while (notFound) {
			if (!(strarray[i].matches(Constants.INTEGER_REGEX))) {

				if (strarray[i].equalsIgnoreCase(firstWord)) {
					nodeList.add(strarray[i]);
				} else {
					nodeList.add(firstWord);
					nodeList.add(strarray[i]);
				}
				notFound = false;
			}
			i--;
		}
		return nodeList;
	}

	/**
	 * takes jsonpath and put them in string array path = $['provider']['reference']
	 * , String array will have [provider,reference] path =
	 * $['diagnosis'][0]['diagnosisReference']['reference'], String array will have
	 * [diagnosis,0,diagnosisReference,reference]
	 */

	public String[] formatreferencepath(String path) {
		StringBuffer sb = new StringBuffer(path);
		sb.delete(0, 2);
		sb.deleteCharAt(sb.length() - 1);
		String temp1 = sb.toString();
		String temp2 = temp1.replace("'", "");
		String temp3 = temp2.replace("][", ":");
		String[] strarray = temp3.split(":");
		return strarray;
	}
}
