package pages;

import configs.loadProps;
import configs.utils;
import java.io.IOException;

/**
 * Page Object class for myaccount functionality.
 * after login user is redirected to my account page and check details and if they want update the personal details
 * 
 * @author Test Engineer
 * @version 1.0
 * @date 2025-12-20
 */
public class myaccount extends utils {

    // Page URL
    public static final String PAGE_URL =loadProps.getProperty("URL") + "https://uksestdevtest02.ukest.lan/MRIEnergy/AdvancedWeb/start-page";
    
    // Page Locators
    public static final String FIRSTNAME = "xpath=//*[@id=\"FirstName\"]";
    public static final String LASTNAME = "xpath=//*[@id=\"Surname\"]";
    public static final String SAVE = "xpath=//*[@id=\"page-buttons\"]/span/button";

    /**
     * Navigates to the myaccount page.
     */
    public static void navigateToMyaccount() {
        System.out.println("🌐 Navigating to myaccount...");
        page.navigate(PAGE_URL);
        page.waitForLoadState();
        System.out.println("✅ myaccount page loaded");
    }

    /**
     * Checks if First Name is visible.
     * 
     * @return true if element is visible
     */
    public static boolean isFirstNameVisible() {
        return isElementPresent(FIRSTNAME);
    }

    /**
     * Checks if LastName is visible.
     * 
     * @return true if element is visible
     */
    public static boolean isLastnameVisible() {
        return isElementPresent(LASTNAME);
    }

    /**
     * Clicks on Save.
     */
    public static void clickSave() {
        System.out.println("🔹 Clicking Save...");
        clickOnElement(SAVE);
        System.out.println("✅ Save clicked");
    }

}
