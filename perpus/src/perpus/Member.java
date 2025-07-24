package perpus;

import java.util.ArrayList;
import java.util.List;

public class Member {
    private int memberId;
    private String name;
    private String phone;
    private List<Loan> loans = new ArrayList<>();

    public Member(int memberId, String name, String phone) {
        this.memberId = memberId;
        this.name = name;
        this.phone = phone;
    }

    public void addLoan(Loan loan) {
        loans.add(loan);
    }

    public List<Loan> getLoans() {
        return loans;
    }
    
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setMemberId(int id) { this.memberId = id; }
    public int getMemberId() { return memberId; }
    public String getName() { return name; }
}

