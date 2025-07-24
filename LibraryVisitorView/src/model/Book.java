package model;

public class Book {
    private int id;
    private String title;
    private String author;
    private String category;
    private String description;
    private String imagePath;
    private boolean available;

    public Book(int id, String title, String author, String category, String description, String imagePath, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.description = description;
        this.imagePath = imagePath;
        this.available = available;
    }

    // Getter & Setter
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public boolean isAvailable() { return available; }

    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setAvailable(boolean available) { this.available = available; }
}

