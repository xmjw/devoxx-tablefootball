package controllers;

import utils.*;
import models.*;
import play.*;
import play.mvc.*;
import views.html.*;
import com.twilio.sdk.verbs.*;
import java.util.*;

public class Application extends Controller {
 
  // This will show the leaderboard. 
  public static Result index() {
    return ok("Hello, world.");
  }
  
  // This will handle all incoming SMS...
  public static Result sms() {
    final Map<String, String[]> values = request().body().asFormUrlEncoded();
    
    String from = values.get("From")[0];
    String to = values.get("To")[0];
    String body = values.get("Body")[0];

    // Now we need to decide what to do with it.
    //  - Is this a request for help?
    //    In which case, send back the help text.
    //  - Does this person have a member record?
    //  - Does this person have a team record?
    //  - Does this person have an unplayed fixture?
    //  - Is this person registering a score?
    
    // Get a member and a status for the message.
    Member member = Member.findByNumber(from);
    String state = new SmsParser(body).getState();
    TwiMLResponse twiml = new TwiMLResponse();
    
    // Run through the different switches...
    
    if (member == null) {
      if (state.equals("join") || state.equals("help")) {
        //new user wants helps. Else, they want to join in the tournament.
        String text = Messages.non_member_help();
        Message sms = new Message();
        try {
          twiml.append(sms);
        } catch (Exception e) {
          
        }
      }
      else {
        //We don't have a member, and they didn't want to do anything that was meaningful.
      }
    }
    else {
      // We have a member...
      
    }

    return ok(twiml.toXML()).as("text/xml");
  }
  
}