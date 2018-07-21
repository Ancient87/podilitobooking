package com.podilito;

import org.jooby.Jooby;
import java.util.concurrent.atomic.AtomicInteger;
import org.jooby.json.Jackson;
import java.util.ArrayList;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author jooby generator
 */
public class App extends Jooby {

    final static int USERID = 69;

    final PaymentEndpoint pe = new PaymentEndpoint();
    final ArrayList<Booking> bookings = new ArrayList<Booking>();

    {
        use(new Jackson());


        path("/bookings", () -> {
            get(req -> { 
                //int userid = req.param("userid").intValue();
                return getBookings();
            });

            post(req -> {
                BookingRequest br = req.params(BookingRequest.class);
                Booking b = makeBooking(USERID, br.bike_id, br.price);
                bookings.add(b);
                return b.getView();
            });
        });

        path("/bookings/demo", () -> {
            get(req -> {
                return demo();
            });
        });

    }

    public static void main(final String[] args) {
        run(App::new, args);
    }

    public ArrayList<BookingView> demo() {
        ArrayList<BookingView> mybookings = new ArrayList<>();
        mybookings.add(new BookingView("abclol", "JGHhFVpkgwbQdiQeSHFxREfUiyHWzYQew3wkFRxeb5MjWTx1oi7", 10, false));
        mybookings.add(new BookingView("abcfoo", "XXXXXXpkgwbQdiQeSHFxREfUiyHWzYQew3wkFRxeb5MjWTx1oi7", 90, true));
        mybookings.add(new BookingView("abcbar", "ZZZZZZpkgwbQdiQeSHFxREfUiyHWzYQew3wkFRxeb5MjWTx1oi7", 5, true));
        mybookings.add(new BookingView("abcbar", "GGGGGpkgwbQdiQeSHFxREfUiyHWzYQew3wkFRxeb5MjWTx1oi7", 900, false));

        return mybookings;

    }

    public ArrayList<BookingView> getBookings() {
        ArrayList<BookingView> mybookings = new ArrayList<>();
        for (Booking b : bookings) {
            mybookings.add(b.getView());
        }

        return mybookings;
    }

    // Make booking and return a booking 
    public Booking makeBooking(int userId, int bikeId, int fee) {
        // TODO: Calculate fee
        //int fee = 5*days;
        // Generate Payment
        int pid = pe.recordPayment(fee).pid;
        //       

        // Write the booking to the DB
        Booking b = new Booking(userId, pid, bikeId, fee);
        bookings.add(b);

        // Return the booking
        return b;
    }

    private class BookingRequest {
        public int bike_id;
        public int price;

        public BookingRequest(int bike_id, int price) {
            this.bike_id = bike_id;
            this.price = price;
        }
    }

    private class Booking {

        public int id;
        public int payRef;
        public int bikeId;
        public int fee;


        public Booking(int id, int payRef, int bikeId, int fee) {
            this.id = id;
            this.payRef = payRef;
            this.bikeId = bikeId;
            this.fee = fee;
        }

        public BookingView getView() {
            // Get the payment info
            PaymentView pv = pe.queryPayment(this.id);
            BookingView bv = new BookingView(pv.ref, pv.wallet, pv.fee, pv.paid);
            
            return bv;
        }
    }

    // PaymentView
    private class PaymentView {
        public boolean paid;
        public int fee;
        public String ref;
        public String wallet;
        public int pid;

    }

    private class PaymentEndpoint {

        private static final String PEURL = "http://localhost:8081/payment";

        public PaymentEndpoint() {


        }

        // Check if it has been paid
        PaymentView queryPayment(int id) {
            PaymentView pv = new PaymentView();
            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.get(PEURL)
                    .header("accept", "application/json")
                    .queryString("id", id)
                    .asJson(); 

                if (jsonResponse.getStatus() == 200) {
                    JsonNode  jn = jsonResponse.getBody();
                    JSONArray ids = jn.getArray();

                    if(ids.length() == 1) {
                        JSONObject obj = ids.getJSONObject(0);
                        pv.paid = obj.getBoolean("paid");
                        pv.fee = obj.getInt("fee");
                        pv.wallet = obj.getString("wallet");
                        pv.ref = obj.getString("ref");
                        return pv;
                    }
                }
            }
            catch (Exception e) {
                System.out.println("That didn't work"+e);
            }
            return null;    
        }


    // Queries payment service API to recordPayment gets the paymentId
    PaymentView recordPayment(int amount) {
            PaymentView pv = new PaymentView();
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.put(PEURL)
                .header("accept", "application/json")
                .field("fee", amount)
                .asJson(); 

            if (jsonResponse.getStatus() == 200) {
                JsonNode  jn = jsonResponse.getBody();
                JSONArray ids = jn.getArray();

                if(ids.length() == 1) {
                    JSONObject obj = ids.getJSONObject(0);
                    pv.paid = obj.getBoolean("paid");
                    pv.fee = obj.getInt("fee");
                    pv.wallet = obj.getString("wallet");
                    pv.ref = obj.getString("ref");
                    return pv;
                }
            }
        }
        catch (Exception e) {
            System.out.println("That didn't work"+e);
        }
        return null;    
    }

}
}
