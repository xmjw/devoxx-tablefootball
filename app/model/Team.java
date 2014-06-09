package models;

import play.*;
import java.util.*;
import utils.*;
import play.db.ebean.*;
import play.data.validation.Constraints.*;
import javax.persistence.*;

@Entity
public class Team extends Model {

  @Id
  public Long id;

  //Name and number of each player, so we can easily find them..
  @OneToMany(mappedBy = "team")
  public List<Member> members;

  //The name of the team.
  @Required
  public String name;

  //Last computed score.
  private int score = -1;
  
  // If this team is playing a game...
  public Boolean playing = false;
  
  // If this team is seeking to play a game.
  public Boolean seeking = true;
  
  //Loose connection to the team you're playing against, to avoid Play 2.1 caching, may or may not be an issue.
  public Long playing_against;

  public static Team CreateTeam() {
    //Always double read a lock...
    if (Team.random == null) {
      synchronized (Team.random_lock) {
        if (Team.random == null) {
          Team.random = new Random();
        }
      } 
    }

    //This is slow, but easier to handle in given the context of the running app. It will go out of scope a lot.
    List<String> teamNames = TeamName.Names();
    List<Team> all_teams = Team.all();
    if (all_teams.size() > 0) Team.removeUsedNames(all_teams, teamNames);

    // Get a random name and allocate it. If the application stops, 
    int index = Team.random.nextInt(teamNames.size());
    Team new_team = new Team();
    new_team.name = teamNames.get(index);

    return new_team;
  }

  // use the factory method instead due to some ebean stuff about accessing tables from an instance of the model
  private Team() {
  }

  // Hold singleton Random, a lock, and team names list in memory.
  private static Random random;
  private static final String random_lock = "LOCKY LOCKY";

  // If we lost the data due to GC, or the Dyno shutting down, clean up this list.
  private static void removeUsedNames(List<Team> existing, List<String> teamNames) {
    for (Team t : existing) {
      teamNames.remove(t.name);
    }
  }

  public static Finder<Long,Team> find = new Finder(
    Long.class, Team.class
  );
  
  public static List<Team> all() {
    return find.all();
  }
  
  public static void create(Team team) {
    team.save();
  }
  
  public static void delete(Long id) {
    find.ref(id).delete();
  }
    
  
  public int computeScore() {
    if (score == -1) {
      if (random == null) random = new Random();
      score = Team.random.nextInt(15);
    }
    return score;
  }   
}
