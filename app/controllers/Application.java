package controllers;

import utils.*;
import models.*;
import play.*;
import play.libs.*;
import play.mvc.*;
import play.data.*;
import views.html.*;
import com.twilio.sdk.verbs.*;
import java.util.*;
import org.codehaus.jackson.node.ObjectNode;


public class Application extends Controller {

  // This will show the leaderboard. 
  public static Result index() {
    return ok(index.render(topFiveTeams(),Pusher.PusherKey()));
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

        List<Team> teams = Team.all();
        Team team = null;
        
        for (Team t : teams) {
          if (t.members.size() == 1) {
            team = t;
          }
        }

        if (team == null) {
          team = Team.CreateTeam();
        }

        team.save();

        Member new_member = new Member();
        new_member.number = from;
        new_member.name = body; //obviously needs imrpoving...
        new_member.team = team;
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

    //Might as well do this whenever we get something happening...
    pushTopFive();
    return ok(twiml.toXML()).as("text/xml");
  }

  private static void pushTopFive() {
    List<Team> teams = topFiveTeams();
   
    //Put each of the teams into a Json object.   
    ObjectNode data = Json.newObject();
    int i=1;
    for (Team team : teams) {
      ObjectNode node = Json.newObject();
      node.put("name", team.name);
      node.put("score", team.computeScore());
      data.put(Integer.toString(i),node);
      i++;
    }
    // Put the set of teams into the data...
    ObjectNode result = Json.newObject();
    result.put("data",data);   
    Logger.info("About to push Score update: "+result.toString());
    Pusher.send(result.toString(),"update_scores");
  }

  //Work out who the top 5 teams are...
  private static List<Team> topFiveTeams() {
    List<Team> teams = Team.all();
    Collections.sort(teams, new TeamComparator());
    int max = (teams.size() > 5 ? 5 : teams.size());
    return teams.subList(0,max);
  }

  //Wrapper to handle errors we twiml.append()
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