package perpus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Loan {
    private Book book;
    private Member member;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
	private int loanId;
	private String memberName;
	private String bookTitle;
	private String status;
	private int memberId;
	private int bookId;

    public Loan(Book book, Member member, LocalDateTime loanDate, LocalDateTime dueDate) {
        this.book = book;
        this.member = member;
        this.loanDate = LocalDateTime.now();
//        this.dueDate = loanDate.plusDays(7).plusMinutes(5);
        this.dueDate = loanDate.plusDays(7); // 7 hari peminjaman
    }

    public Loan(int loanId, int memberId, String memberName, int bookId, String bookTitle,
    		LocalDateTime loanDate, LocalDateTime dueDate, String status) {
    this.loanId = loanId;
    this.memberId = memberId;
    this.memberName = memberName;
    this.bookId = bookId;
    this.bookTitle = bookTitle;
    this.loanDate = loanDate;
    this.dueDate = dueDate;
    this.status = status;
}

	public boolean isOverdue() {
        return LocalDateTime.now().isAfter(dueDate);
    }

    // Getter methods
    public Book getBook() { return book; }
    public Member getMember() { return member; }
    public LocalDateTime getLoanDate() { return loanDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public int getLoanId() { return loanId; }
    public String getMemberName() { return memberName; }
    public String getBookTitle() { return bookTitle; }
    public String getStatus() { return status; }
    public int getMemberId() {
        return memberId;
    }
    public int getBookId() {
        return bookId;
    }
    
}

