package utils;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import com.twilio.sdk.resource.list.MessageList;
import java.util.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import models.*;
import play.*;

public class TwilioNotifier {
  
  private static String TwilioSid() {
    return System.getenv().get("TWILIO_SID");
  }

  private static String TwilioToken() {
    return System.getenv().get("TWILIO_TOKEN");
  }
  
  private static String TwilioNumber() {
    return System.getenv().get("TWILIO_NUMBER");
  }
  
  private static boolean SendSms(String to, String message) {
    
    try {
      TwilioRestClient client = new TwilioRestClient(TwilioSid(), TwilioToken());
 
      // Build a filter for the MessageList
      List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("Body", message));
      params.add(new BasicNameValuePair("To", to));
      params.add(new BasicNameValuePair("From", TwilioNumber()));
 
      MessageFactory messageFactory = client.getAccount().getMessageFactory();
      //Message sms = messageFactory.create(params);
      //Logger.info("SMS To:"+to+" | "+sms.getSid()" | "+message+" |");

      Logger.info("SMS To:"+to+" | "+message+" |");
      return true;
    }
    catch (Exception e) {
      Logger.error("Twilio raised an exception trying to send a message.",e);
      return false;
    }
  }

  private static void NotifyMember(Member member, String message) {
    SendSms(member.number, message);
  }

  private static void NotifyTeam(Team team, String message) {
    for(Member member : team.members) {
      NotifyMember(member,message);
    }    
  }

  public static void Session(List<Member> members) {
    for(Member member : members) {
      NotifyMember(member,Messages.Session());
    }    
  }

  public static void Loss(Team team, Team against) {
    NotifyTeam(team,Messages.Loss(against));    
  }

  public static void Win(Team team, Team against) {
    NotifyTeam(team,Messages.Win(against));
  }

  public static void Draw(Team team, Team against) {
    NotifyTeam(team,Messages.Draw(against));
    NotifyTeam(against,Messages.Draw(team));
  }

  public static void Play(Team team, Team against) {
    NotifyTeam(team,Messages.Play(against));
  }

  public static void NoPlay(Team team) {
    NotifyTeam(team,Messages.NoPlay());
  }
  
  public static void ScorePending(Team team) {
    NotifyTeam(team,Messages.ScorePending());
  }
  
  public static void Abort(Team team) {
    NotifyTeam(team,Messages.Abort());
  }

  public static void NewTeam(Team team) {
    NotifyTeam(team, Messages.NewTeam(team));
  }

  public static void TeamWaiting(Member member) {
    NotifyMember(member, Messages.TeamWaiting());
  }

  public static void NonMemberHelp(String from) {
    SendSms(from,Messages.NonMemberHelp());
  }
  
  public static void MemberHelp(Member member) {
    NotifyMember(member,Messages.MemberHelp());
  }
}