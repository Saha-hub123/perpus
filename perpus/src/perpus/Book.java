package perpus;

public class Book {
    private int id;
    private String imagePath;
    private String title;
    private String author;
    private String category;
    private String description;
    private boolean isAvailable;

    public Book(int id, String title, String author, String imagePath, String category, String description, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.imagePath = imagePath;
        this.category = category;
        this.description = description;
        this.isAvailable = available;
    }

    // Getter & Setter methods
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return isAvailable; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void borrowBook() { isAvailable = false; }
    public void returnBook() { isAvailable = true; }
}

