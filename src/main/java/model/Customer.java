package model;

public class Customer {
    private int customerId;
    private String firstName;
    private String lastName;
    private String birthDate;
    private String bloodStatus; // pure, half, muggle
    private String house;       // Hogwarts house
    private String species;     // human, half-giant, etc.
    private String wandLicense;
    private String notes;
    private String registrationDate;


    public Customer(String firstName, String lastName, String birthDate, String bloodStatus, String house, String species, String wandLicense, String notes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.bloodStatus = bloodStatus;
        this.house = house;
        this.species = species;
        this.wandLicense = wandLicense;
        this.notes = notes;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getBloodStatus() {
        return bloodStatus;
    }

    public void setBloodStatus(String bloodStatus) {
        this.bloodStatus = bloodStatus;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getWandLicense() {
        return wandLicense;
    }

    public void setWandLicense(String wandLicense) {
        this.wandLicense = wandLicense;
    }
}