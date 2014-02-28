package zap;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale.Category;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GiftZappos {
	double X;
	private List<Double> comboPrice;		//Stores list of total price of combo
	private List<String> comboList;			//Stores list of combos
	private List<String> availItems;		//Name of the available products in choosen category 
	private List<Double> itemPrice;			//Price of the available products in choosen category
	private List<Double> closeness;			//Stores closeness of each combo with input dollar amount

	public GiftZappos(double X) {
		this.X = X;
		comboList = new ArrayList<>();
		comboPrice = new ArrayList<>();
		closeness = new ArrayList<>();
		availItems = new ArrayList<>();
		itemPrice = new ArrayList<>();
	}

	
	/**
	 * This method creates all unique possible combinations of available products and returns a Set of Set containing combinations
	 * @param products
	 * @param noOfItems		number of items in a combo
	 * @return
	 */
	private static Set<Set<String>> makeCombo(List<String> products, int noOfItems) {
		Set<Set<String>> itemCombo = new HashSet<>();
		if (noOfItems == 0) {
			itemCombo.add(new HashSet<String>());
		} else if (noOfItems <= products.size()) {
			List<String> items = new ArrayList<>(products);
			String last = items.remove(items.size() - 1);

			Set<Set<String>> part1 = makeCombo(items, noOfItems);
			Set<Set<String>> part2 = makeCombo(items, noOfItems - 1);

			for (Set<String> combination : part2) {
				combination.add(last);
			}
			itemCombo.addAll(part1);
			itemCombo.addAll(part2);
		}
		return itemCombo;
	}

	/**
	 * This method converts Set of Set of combos obtained from makeCombo() to a List
	 * @param itemCombos
	 */
	private void makeList(Set<Set<String>> itemCombos) {
		for (Set<String> combo : itemCombos) {
			String temp = "";
			for (String str : combo) {
				temp = temp + str.trim() + "#";
			}
			if (!comboList.contains(temp))
				comboList.add(temp);
		}
	}

	/**
	 * This method populates comboPrice list with corresponding combo present in comboList 
	 */
	private void calcPriceCombo() {
		System.out.println("*** Calculating Price of Combos ***");
		for (String combo : comboList) {
			String[] itemArr = combo.split("#");
			Double price = 0.0;
			for (String item : itemArr) {
				int indx = availItems.indexOf(item);
				price += itemPrice.get(indx);
			}
			comboPrice.add(price);
		}
		
		//Calculating closeness of each combination with DollarAmount
		for (Double cp : comboPrice) {
			closeness.add(Math.abs(X - cp));
		}
	}

	/**
	 * This method returns indices of top n minimum values present in closeness List.
	 * The values present in comboList and comboPrice at corresponding indices will be closest to dollar amount.
	 * @param n		number of combos to be displayed
	 * @return
	 */
	private List<Integer> getClosestIndices(int n) {
		int k = 0;
		List<Integer> indexList = new ArrayList<>();
		List<Double> closeCopy = new ArrayList<>();
		closeCopy.addAll(closeness);
		int size = closeness.size();
		for (int i = 0; i < size; i++) {
			Double minVal = Collections.min(closeCopy);
			int index = closeness.indexOf(minVal);
			if (!indexList.contains(index))
				indexList.add(index);
			closeCopy.remove(minVal);
			closeness.set(index, -1.0);
			if (indexList.size() == n)
				break;
		}
		return indexList;
	}

	/**
	 * This method calls Zappos api and extracts category names (Shoes, Clothing, Bags etc.)
	 * and returns it in the form of a List.
	 * @param url	url to call api giving category names
	 * @return
	 * @throws IOException
	 */
	public List<String> getCategories(URL url) throws IOException {
		List<String> categories = new ArrayList<>();
		InputStream is = url.openStream();
		JsonParser parser = Json.createParser(is);
		while (parser.hasNext()) {
			Event event = parser.next();
			if (event == Event.KEY_NAME ) {
				
				switch (parser.getString()) {
				case "facets":
					while(true){
						if(event == Event.KEY_NAME && parser.getString().equals("facetField")){
							break;
						}
						if (event == Event.KEY_NAME && parser.getString().equals("name"))
						{
								parser.next();
								System.out.println(parser.getString());
								categories.add(parser.getString());
						}
						event = parser.next();
					}
				}
				break;
			}
		}
		return categories;
	}
	
	/**
	 * This method calls the Zappos api and extracts product names and prices of available items in chosen category.
	 * @param url
	 * @throws IOException
	 */
	public void getProductDetails(URL url) throws IOException {
		// URL url = new URL(urlString);
		boolean prodAdded = true;
		InputStream is = url.openStream();
		JsonParser parser = Json.createParser(is);
		while (parser.hasNext()) {
			Event event = parser.next();
			if (event == Event.KEY_NAME) {
				switch (parser.getString()) {
				case "price":
					parser.next();
					String priceStr = parser.getString().substring(1);
					Double price = Double.parseDouble(priceStr);
					if (prodAdded) {
						itemPrice.add(price);
						prodAdded = false;
					}
					break;
				case "productName":
					parser.next();
					String prodName = parser.getString().trim();
					if (!(availItems.contains(prodName))) {
						availItems.add(prodName);
						prodAdded = true;
					}
					break;
				}
			}
		}
	}

	public static void main(String[] args) {
		int noOfItems = 0;
		Double dollarAmount = 140.0;
		boolean validInput = false;
		String strN = null;
		String strDollar = null;
		String key = "a73121520492f88dc3d33daf2103d7574f1a3166";
		URL url = null;
		
		//User input - No. of items
		while (!validInput) {
			strN = JOptionPane.showInputDialog(null, "Enter Number of Products (Cancel to exit): ", "", JOptionPane.PLAIN_MESSAGE);
			try {
				if(strN == null)
					System.exit(0);
				noOfItems = Integer.parseInt(strN);
				validInput = true;
			} catch (NumberFormatException nfe) {
				validInput = false;
			}
		}
		validInput = false;
		
		//User input - Dollar Amount
		while(!validInput) {
			strDollar = JOptionPane.showInputDialog(null, "Enter Dollar Amount (Cancel to exit): ", "", 1);
			if(strDollar==null)
				System.exit(0);
			try{
				dollarAmount = Double.parseDouble(strDollar);
				if(dollarAmount.isNaN())
					validInput = false;
				else
					validInput = true;
			}catch (NumberFormatException nfe){
				validInput = false;
			}
		}
		
		GiftZappos gift = new GiftZappos(dollarAmount);
		List<String> categories = new ArrayList<>();
		categories.add("Clothing"); 	//Default value
		
		//Calling getCategories() to populate Category dropdown.
		try {
			url = new URL("http://api.zappos.com/Search?key=" + key + "&includes=[%22facets%22]");
			categories = gift.getCategories(url);
		} catch (MalformedURLException e1) {
			System.err.println("Malformed URL");
		} catch (IOException e) {
			System.err.println("Error Calling API");
		}
		
		String category = (String)JOptionPane.showInputDialog(null, "Select Category (Cancel to exit):", "Categories", JOptionPane.PLAIN_MESSAGE, null, categories.toArray(), "Clothing");
		if(category == null)
			System.exit(0);
		
		//Calling getProductDetails()
		try {
			url = new URL("http://api.zappos.com/Search?term=" + category + "&key=" + key);
			gift.getProductDetails(url);
		} catch (MalformedURLException e) {
			System.err.println("Malformed URL");
		} catch (IOException e) {
			System.err.println("Error Calling API");
			e.printStackTrace();
		}

		
		List<String> itemList = new ArrayList<>();
		itemList.addAll(gift.availItems);
		
		//Calling makeCombo to form product combinations
		System.out.println("*** Making Combos ***");
		Set<Set<String>> itemCombos = makeCombo(itemList, noOfItems);
		
		//Calling makeList to convert Set of Set of combos to List of combos
		gift.makeList(itemCombos);
		
		//Calling calcPriceCombo to populate comboPrice and closeness lists
		gift.calcPriceCombo();
		
		//Finding indices of closest combinations
		List<Integer> indices = gift.getClosestIndices(noOfItems);
		
		//Based on indices printing combinations and prices
		for (Integer index : indices) {
			System.out.println(gift.comboList.get(index).replaceAll("#", ",").substring(0, gift.comboList.get(index).length() - 1)
					+ ": "
					+ gift.comboPrice.get(index));
		}
	}
}
