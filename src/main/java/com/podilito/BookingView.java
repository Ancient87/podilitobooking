
package com.podilito;
import org.jooby.Jooby;
import java.util.concurrent.atomic.AtomicInteger;
import org.jooby.json.Jackson;


public class BookingView {
     public int id;
     public String reference;
     public String wallet;
     public int fee;
     public boolean paid;

     static AtomicInteger idgen = new AtomicInteger();

     public BookingView(final String reference, final String wallet, int fee, boolean paid) {
         this.id = idgen.incrementAndGet();
         this.reference = reference;
         this.wallet = wallet; 
         this.fee = fee;
         this.paid = paid;
     }
 }

