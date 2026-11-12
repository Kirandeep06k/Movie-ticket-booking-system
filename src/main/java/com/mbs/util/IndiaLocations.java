package com.mbs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** States / union territories of India with their main cities (HashMap-style data in a TreeMap). */
public final class IndiaLocations {
    private static final Map<String, List<String>> DATA = new TreeMap<>();

    static {
        add("Andhra Pradesh", "Visakhapatnam", "Vijayawada", "Guntur", "Tirupati", "Nellore", "Kurnool");
        add("Arunachal Pradesh", "Itanagar", "Naharlagun", "Pasighat", "Tawang");
        add("Assam", "Guwahati", "Silchar", "Dibrugarh", "Jorhat", "Nagaon");
        add("Bihar", "Patna", "Gaya", "Bhagalpur", "Muzaffarpur", "Darbhanga");
        add("Chhattisgarh", "Raipur", "Bhilai", "Bilaspur", "Korba");
        add("Goa", "Panaji", "Margao", "Vasco da Gama", "Mapusa");
        add("Gujarat", "Ahmedabad", "Surat", "Vadodara", "Rajkot", "Gandhinagar", "Bhavnagar");
        add("Haryana", "Gurugram", "Faridabad", "Panipat", "Ambala", "Karnal", "Hisar");
        add("Himachal Pradesh", "Shimla", "Manali", "Dharamshala", "Solan", "Mandi");
        add("Jharkhand", "Ranchi", "Jamshedpur", "Dhanbad", "Bokaro");
        add("Karnataka", "Bengaluru", "Mysuru", "Mangaluru", "Hubballi", "Belagavi");
        add("Kerala", "Thiruvananthapuram", "Kochi", "Kozhikode", "Thrissur", "Kollam");
        add("Madhya Pradesh", "Bhopal", "Indore", "Gwalior", "Jabalpur", "Ujjain");
        add("Maharashtra", "Mumbai", "Pune", "Nagpur", "Nashik", "Aurangabad", "Thane");
        add("Manipur", "Imphal", "Thoubal", "Churachandpur");
        add("Meghalaya", "Shillong", "Tura", "Jowai");
        add("Mizoram", "Aizawl", "Lunglei", "Champhai");
        add("Nagaland", "Kohima", "Dimapur", "Mokokchung");
        add("Odisha", "Bhubaneswar", "Cuttack", "Rourkela", "Puri", "Sambalpur");
        add("Punjab", "Ludhiana", "Amritsar", "Jalandhar", "Patiala", "Mohali", "Bathinda");
        add("Rajasthan", "Jaipur", "Jodhpur", "Udaipur", "Kota", "Ajmer", "Bikaner");
        add("Sikkim", "Gangtok", "Namchi", "Gyalshing");
        add("Tamil Nadu", "Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Salem");
        add("Telangana", "Hyderabad", "Warangal", "Nizamabad", "Karimnagar");
        add("Tripura", "Agartala", "Dharmanagar", "Kailashahar");
        add("Uttar Pradesh", "Lucknow", "Kanpur", "Varanasi", "Agra", "Noida", "Prayagraj", "Ghaziabad");
        add("Uttarakhand", "Dehradun", "Haridwar", "Haldwani", "Rishikesh", "Nainital");
        add("West Bengal", "Kolkata", "Howrah", "Durgapur", "Siliguri", "Asansol");
        // union territories
        add("Andaman and Nicobar Islands", "Port Blair");
        add("Chandigarh", "Chandigarh");
        add("Dadra and Nagar Haveli and Daman and Diu", "Daman", "Diu", "Silvassa");
        add("Delhi", "New Delhi", "Dwarka", "Rohini", "Saket");
        add("Jammu and Kashmir", "Srinagar", "Jammu", "Anantnag", "Baramulla");
        add("Ladakh", "Leh", "Kargil");
        add("Lakshadweep", "Kavaratti");
        add("Puducherry", "Puducherry", "Karaikal");
    }

    private IndiaLocations() { }

    private static void add(String state, String... cities) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, cities);
        DATA.put(state, list);
    }

    public static List<String> states() { return new ArrayList<>(DATA.keySet()); }

    public static List<String> cities(String state) {
        List<String> list = state == null ? null : DATA.get(state);
        return list == null ? new ArrayList<String>() : new ArrayList<>(list);
    }
}
