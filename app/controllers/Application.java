package controllers;

import utils.*;
import models.*;
import play.*;
import play.libs.*;
import play.mvc.*;
import play.data.*;
import views.html.*;
import play.db.ebean.Transactional;
import com.twilio.sdk.verbs.*;
import java.util.*;
import org.codehaus.jackson.node.ObjectNode;


public class Application extends Controller {

  public static Result session_alert() {
    TwilioNotifier.Session(Member.all());
    return ok("Sent...");
  }

  public static Result report() {
    return ok(report.render(Team.all()));
  }

  // This will show the leaderboard. 
  public static Result index() {
    return ok(index.render(topFiveTeams(),Pusher.PusherKey()));
  }

  @Transactional
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
    return ok("Deleted "+items+" items from the database.");    
  }

  @Transactional
  private static Team find_or_create_team() {
    List<Team> teams = Team.all();
    Team team = null;
    
    //find an empty team...
    for (Team t : teams) {
      if (t.members.size() == 1) {
        team = t;
        Logger.info("Found a team with only one member.");
      }
    }

    //nope. Create a team.
    if (team == null) {
      Logger.info("No teams are available, so creating a new one.");
      team = Team.CreateTeam();
    }
    team.save();
    
    // return the team...
    return team;
  }

  @Transactional
  private static Member create_member(Member member, String from) {
    Logger.info("Creating a new member and allocating the team.");
    Team team = find_or_create_team();
    
    member = new Member();
    member.number = from;
    member.team = team;
    team.members.add(member);
    member.save();
    team.save();
    return member;    
  }

  private static void NoMember(Member member,State state) {
    //Is person asking for help?
    if (state.is("join")) {
      member = create_member(member, state.from);
      Team team = member.team;
      //Create a member. Can we put it in a team?
      if (team.members.size() == 2) {
        Logger.info("Notifying team members that it's time to party.");
        TwilioNotifier.NewTeam(member.team);
      }
      else {
        Logger.info("Notifying the member that they'll have to wait...");
        TwilioNotifier.TeamWaiting(member);
        
      }
    }
    else {
      TwilioNotifier.NonMemberHelp(state.from);
    }
  }

  @Transactional
  private static void MemberPlay(Member member) {
    //Find a team that can be played, and isn't this one.
    Logger.info("Initiating a game for the member...");
    List<Team> teams = Team.all();
    Team challenged = null;
    Team team = member.team;
    for (Team t : teams) {
      if (t.seeking && t.members != null && t.members.size() == 1) {
        challenged = t;
      }
    } 
    Logger.info("Check to see if we have a challenger...");
    if (challenged != null) {
      Logger.info("Saving play state.");

      team.play(challenged);
      team.save();

      challenged.play(team); 
      challenged.save();

      Logger.info("Notifying...");
      TwilioNotifier.Play(team,challenged);
      TwilioNotifier.Play(challenged,team);
    }
    else {
      TwilioNotifier.NoPlay(team);
    }
  }

  @Transactional
  private static void GameOver(Team winner, Team loser) {
    winner.won();
    winner.save();
    loser.lost();
    loser.save();
    TwilioNotifier.Win(winner, loser);
    TwilioNotifier.Loss(loser, winner);
  }

  @Transactional
  private static void GameDraw(Team team, Team against) {
    team.tempScore = 0;
    team.save();
    against.tempScore = 0;
    against.save();
    TwilioNotifier.Draw(team,against);
  }

  @Transactional
  private static void MemberScore(Member member, State state) {
    // Log a score for a game.
    Team team = member.team;
    Team against = Team.find.byId(team.playing_against);

    team.tempScore = state.data;
    team.save();

    Logger.info("Reporting Team Indicates "+Integer.toString(state.data)+" (recorded as "+Integer.toString(team.tempScore)+")");
    Logger.info("Against Team recorded score as "+Integer.toString(against.tempScore)+")");

    //Both teams have registered a score!
    if (against.tempScore != -1) {
      if (team.tempScore > against.tempScore) GameOver(team,against); //Member wins.
      else if (team.tempScore < against.tempScore) GameOver(against,team); //Against wins.
      else GameDraw(team, against); // a draw!
    }
    else {
      TwilioNotifier.ScorePending(team);
    }
  }

  @Transactional
  private static void MemberAbort(Member member) {
    //Something went wrong. Cancel and reset to non-game playing.
    Team team = member.team;
    if (team.playing) {
      Team against = Team.find.byId(team.playing_against);
      team.abort();
      team.save();
      against.abort();
      against.save();
      TwilioNotifier.Abort(team);
      TwilioNotifier.Abort(against);
    }
  }

  private static void Member(Member member, State state) {
    //Already a member, and we have that in variable member.
    if (state.is("play")) {
      MemberPlay(member);
    }
    else if (state.is("score")) {
      MemberScore(member,state);
    }
    else if (state.is("abort")) {
      MemberAbort(member);
    }
    else {
      TwilioNotifier.MemberHelp(member);
    } 
  }

  private static void GameLogic(Member member,State state) {
    if (member == null) {
      NoMember(member, state);
    }
    else {
      Member(member, state);
    }
  }

  public static boolean active() {
    String state =  System.getenv().get("ACTIVE");
    return (!state.equals("FALSE"));
  }

  // This will handle all incoming SMS...
  public static Result sms() {
    
    if (active() == false) {
      return ok("<Response><Message>Sorry, the tournament is now closed. Check out http://www.twilio.com to see how we built it with Twilio's API.</Message></Response>").as("text/xml");      
    }
    
    //final Map<String, String[]> values = request().body().asFormUrlEncoded();
    DynamicForm requestData = form().bindFromRequest();
    String from = requestData.get("From");
    String to   = requestData.get("To");
    String body = requestData.get("Body");

    Logger.info("We have just received some data! From: '"+from+"' To: '"+to+"' Body: '"+body+"'.");

    // Get a member and a status for the message.
    Member member = Member.findByNumber(from);
    State state = new SmsParser(body, from).getState();
    TwiMLResponse twiml = new TwiMLResponse();
    
    Logger.info("Resolved State: "+state.state);
    
    // Handle everything..
    GameLogic(member,state);
    
    //Might as well do this whenever we get something happening...
    pushTopFive();
    
    //To simplify branching we will send SMS notifications with the REST API.
    return ok("<Response></Response>").as("text/xml");
  }

  private static void pushTopFive() {
    List<Team> teams = topFiveTeams();
   
    //Put each of the teams into a Json object.   
    ObjectNode data = Json.newObject();
    int i=1;
    for (Team team : teams) {
      ObjectNode node = Json.newObject();
      node.put("name", team.name);
      node.put("score", team.wins);
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
    List<Team> pairedTeams = new ArrayList<Team>();
    for (Team t : teams) {
      if (t.members.size() == 2) pairedTeams.add(t);
    }
    Collections.sort(pairedTeams, new TeamComparator());    
    int max = (pairedTeams.size() > 5 ? 5 : pairedTeams.size());
    return pairedTeams.subList(0,max);
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