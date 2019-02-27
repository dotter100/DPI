package loanbroker;

import model.loan.LoanReply;

import java.util.Comparator;

public class IntrestCompare implements Comparator<LoanReply> {


    @Override
    public int compare(LoanReply o1, LoanReply o2) {
        return Double.compare(o1.getInterest(), o2.getInterest());
    }


}
