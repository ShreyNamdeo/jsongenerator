package com.jsonUtility.jsonCreator.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonFileDto {
    private String id;
    private String CategoriesSpecific;
    private String Subject;
    private String StartDate;
    private String StartTime;
    private String EndDate;
    private String EndTime;
    private String Alldayevent;
    private String Categories;
    private String WikipediaURL;
    private String GreetingCardURL;
    private String Description;
    private String Countries;
    private String Religion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoriesSpecific() {
        return CategoriesSpecific;
    }

    public void setCategoriesSpecific(String categoriesSpecific) {
        CategoriesSpecific = categoriesSpecific;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getEndDate() {
        return EndDate;
    }

    public void setEndDate(String endDate) {
        EndDate = endDate;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public String getAlldayevent() {
        return Alldayevent;
    }

    public void setAlldayevent(String alldayevent) {
        Alldayevent = alldayevent;
    }

    public String getCategories() {
        return Categories;
    }

    public void setCategories(String categories) {
        Categories = categories;
    }

    public String getWikipediaURL() {
        return WikipediaURL;
    }

    public void setWikipediaURL(String wikipediaURL) {
        WikipediaURL = wikipediaURL;
    }

    public String getGreetingCardURL() {
        return GreetingCardURL;
    }

    public void setGreetingCardURL(String greetingCardURL) {
        GreetingCardURL = greetingCardURL;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getCountries() {
        return Countries;
    }

    public void setCountries(String countries) {
        Countries = countries;
    }

    public String getReligion() {
        return Religion;
    }

    public void setReligion(String religion) {
        Religion = religion;
    }
}
