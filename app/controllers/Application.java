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

  private static Team find_or_create_team() {
    List<Team> teams = Team.all();
    Team team = null;
    
    //find an empty team...
    for (Team t : teams) {
      if (t.members.size() == 1) {
        team = t;
      }
    }

    //nope. Create a team.
    if (team == null) {
      team = Team.CreateTeam();
    }
    team.save();
    
    // return the team...
    return team;
  }

  private static Team create_member(Member member, String body, String from) {
    member = new Member();
    member.number = from;
    member.name = body; //obviously needs imrpoving...
    member.team = find_or_create_team();
    member.save();
    return member;    
  }

  private static String decision_tree(Member member,String state, String body, String from) {
    if (member == null) {
      //Is person asking for help?
      if state.equals("help"){
        return "new_help";
      }
      //Create a member. Can we put it in a team?
      else if (state.equals("join")) {

        if (member.team.size() == 2) {
          return "new_team"
        }
        else {
          return "team_waiting"
        }
      }
    }
    else {
      //Already a member, and we have that in variable member.
      if (state.equals("play")) {
        
      }
      else if (state.equals("challenge")) {
        
      }
      else if (state.equals("accept")) {
        
      }
      else if (state.equals("score")) {
        
      }
      else if (state.equals("abort")) {
        
      }
      else
        return "existing_help";
      }
      
    }

    if (member == null) {
      if (state.equals("join") || state.equals("help")) {
        //new user wants helps. Else, they want to join in the tournament.
        String text = Messages.non_member_help();
        Message message = new Message("string");



      }
      else {
        //We don't have a member, and they didn't want to do anything that was meaningful.
        String text = Messages.non_member_help();
        Message message = new Message(text);
      }
    }
    else {
      // We have a member...
      String text = Messages.member_help();
      Message message = new Message(text);
    }


    return "help";
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
    String branch = decision_tree(member,state, body, from);
    
    switch(branch) {
      case "help":
      default: 
        Logger.info("Default out of branch.");
        break;
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