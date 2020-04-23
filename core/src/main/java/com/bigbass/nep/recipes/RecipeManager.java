package com.bigbass.nep.recipes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javax.json.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.bigbass.nep.recipes.delivery.Importer;
import com.bigbass.nep.recipes.delivery.PeerConfig;
import com.bigbass.nep.recipes.elements.AElement;
import com.bigbass.nep.recipes.processing.Recipe;

public class RecipeManager {
	
	private static RecipeManager instance;
	
	/**
	 * <p>Table of recipes.</p>
	 * 
	 * <p>Please take care when adding recipes! The key is NOT the source name from the JSON data, but rather the
	 * name of the crafting/process in which the recipe goes. So for a Gregtech recipe in the Compressor,
	 * the key would be "Compressor", and all compressor recipes (in a list) would be the key's value.</p>
	 */
	public Hashtable<String, List<Recipe>> recipes;
	
	/**
	 * Contains a list of sources as defined by the JSON data. Each source's value is a list that contains
	 * the keys from {@link RecipeManager#recipes} that originate from the source.
	 */
	public Hashtable<String, List<String>> recipeSources;
	
	private RecipeManager(){
		recipes = new Hashtable<String, List<Recipe>>();
		recipeSources = new Hashtable<String, List<String>>();
	}
	
	public static RecipeManager getInst(){
		if(instance == null){
			instance = new RecipeManager();
		}
		
		return instance;
	}

	/**
	 * <p>Flushes any currently loaded recipes and attempts to load the specified version.</p>
	 * 
	 * <p>If the version is not found locally and {@code localOnly == false}, it will attempt to download the
	 * respective json file. If the version does not exist on the remote server, a RecipeError will be thrown.</p>
	 * 
	 * @param version recipe version to be loaded
	 * @param localOnly true if this version is only found locally
	 * @return
	 */
	public RecipeError loadRecipes(PeerConfig config) throws FileNotFoundException {
		final FileHandle handle = Gdx.files.local(Importer.getRoot() + config.alias + '/');

		JsonReader mendeleyReader = Json.createReader(new FileReader(handle.file().getPath() + "/mendeley.json"));
		for (JsonValue rawElem : mendeleyReader.readArray()) {
			AElement.fromJson(rawElem.asJsonObject());
		}

		final File sources = new File(handle.file().getPath() + "/sources/");

		for (File source : sources.listFiles()) {
			String sourceType = source.getName();

			if(sourceType.equalsIgnoreCase("gregtech")) {
				final File gregTechFolder = new File(source.getPath() + "/");

				ArrayList<String> machineNames = new ArrayList<>();

				for(File machine : gregTechFolder.listFiles()) {
					final List<Recipe> machineRecipes = new ArrayList<>();
					final String machineName = machine.getName().replace(".json", "");
					JsonReader reader = Json.createReader(new FileReader(machine.getPath()));

					for(JsonValue jr : reader.readArray()){
						JsonObject jsonRecipe = jr.asJsonObject();
						if(!jsonRecipe.isEmpty()){
							Recipe recipe = Recipe.fromJson(jsonRecipe, machineName);
							machineRecipes.add(recipe);
						}
					}

					if(!machineRecipes.isEmpty()){
						recipes.put(machineName, machineRecipes);
						machineNames.add(machineName);
					}
				}
				recipeSources.put(sourceType, machineNames);

			} else if (!sourceType.equals("shapedOreDict")) {
				final List<String> craftingType = new ArrayList<>(1);
				craftingType.add(sourceType);
				recipeSources.put(sourceType, craftingType);

				JsonReader reader = Json.createReader(new FileReader(source.getPath() + "/recipes.json"));

				final List<Recipe> shapedRecipes = new ArrayList<>();
				for(JsonValue jr : reader.readArray()) {
					JsonObject jsonRecipe = jr.asJsonObject();
					if(!jsonRecipe.isEmpty()) {
						Recipe recipe = Recipe.fromJson(jsonRecipe, sourceType);
						shapedRecipes.add(recipe);
					}
				}
				recipes.put(sourceType, shapedRecipes);
			} //TODO oredicted recipes
		}
		return null; // intended; returning null means no errors occured
	}
	
	/**
	 * Attempts to locate a particular recipe by comparing the provided hashCode with the hashCode of every loaded recipe.
	 * 
	 * This function may cause some lag depending on how fast it finds the recipe.
	 * 
	 * @param hashCode of the recipe
	 * @return the found recipe, or null
	 */
	public Recipe findRecipe(int hashCode){
		if(hashCode == -1){
			return null;
		}
		
		if(recipes != null){
			for(String key : recipes.keySet()){
				for(Recipe rec : recipes.get(key)){
					if(rec.hashCode() == hashCode){
						return rec;
					}
				}
			}
		}
		
		return null;
	}
	
	
	public class RecipeError {
		
		private final String code;
		private final String description;
		
		public RecipeError(String code, String description){
			this.code = code;
			this.description = description;
		}
		
		public String getCode(){
			return code;
		}
		
		public String getDescription(){
			return description;
		}
		
		@Override
		public String toString(){
			return code + ": " + description;
		}
	}
}
