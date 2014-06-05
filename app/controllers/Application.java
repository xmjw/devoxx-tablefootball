package controllers;

import utils.*;
import models.*;
import play.*;
import play.mvc.*;
import play.data.*;
import views.html.*;
import com.twilio.sdk.verbs.*;
import java.util.*;



public class Application extends Controller {
 
  // This will show the leaderboard. 
  public static Result index() {
    //Pusher.send("{\"data\":\"{\\\"message\\\":\\\"DOUBLE CHECKING....\\\"}}","my_event");
    return ok(index.render(Team.all(),Pusher.PusherKey()));
  }

  public static Result delete() {
    int items = 0;
    for (Member m : Member.all()) {
      items++;
      m.delete();
    }
    for (Team m : Team.all()) {
      items++;
      m.delete();
    }
    for (Game m : Game.all()) {
      items++;
      m.delete();
    }
    for (Fixture m : Fixture.all()) {
      items++;
      m.delete();
    }
    return ok("Deleted "+items+" items from the database.");    
  }
  
  // This will handle all incoming SMS...
  public static Result sms() {
    //final Map<String, String[]> values = request().body().asFormUrlEncoded();
    DynamicForm requestData = form().bindFromRequest();
    String from = requestData.get("From");
    String to   = requestData.get("To");
    String body = requestData.get("Body");
    
    Logger.info("We have just received some data! From: '"+from+"' To: '"+to+"' Body: '"+body+"'.");
    
    //String from = values.get("From")[0];
    //String to = values.get("To")[0];
    //String body = values.get("Body")[0];

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
    if (member == null)
      Logger.info("Member is null.");
    else
      Logger.info("Member has an inexplicable value...");
      
    Logger.info("Got a state: "+state);
    
    if (member == null) {
      if (state.equals("join") || state.equals("help")) {
        //new user wants helps. Else, they want to join in the tournament.
        String text = Messages.non_member_help();
        Message message = new Message("string");
        twiml = safelyAppendElement(twiml, message);      

        Team new_team = new Team();
        new_team.name = "Some team.";
        new_team.save();
        
        Member new_member = new Member();
        new_member.number = from;
        new_member.name = body; //obviously needs imrpoving...
        new_member.team = new_team;
        new_member.save();
        
      }
      else {
        //We don't have a member, and they didn't want to do anything that was meaningful.
        String text = Messages.non_member_help();
        Message message = new Message(text);
        twiml = safelyAppendElement(twiml, message);
      }
    }
    else {
      // We have a member...
      String text = Messages.member_help();
      Message message = new Message(text);
      twiml = safelyAppendElement(twiml, message);      
    }

    return ok(twiml.toXML()).as("text/xml");
  }

  private static TwiMLResponse safelyAppendElement(TwiMLResponse twiml, Verb verb) {
    try {
      twiml.append(verb);
    } 
    catch (Exception e) {
      Logger.error("Encountered a problem adding a Verb ("+verb.toXML()+") to the Response.");
    }
    return twiml;
  }
  
}