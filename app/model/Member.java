package models;

import java.util.*;

import play.db.ebean.*;
import play.data.validation.Constraints.*;
import java.sql.Timestamp;
import javax.persistence.*;

@Entity
public class Member extends Model {

  @Id
  public Long id;
  
  @Version
  public Timestamp lastUpdate;
      
  @ManyToOne(cascade = CascadeType.PERSIST)
  public Team team;
  
  @Required
  public String number;
  
  public static Finder<Long,Member> find = new Finder(
    Long.class, Member.class
  );
  
  public static Member findByNumber(String number) {
    List<Member> results = find.where().eq("number",number).findList();
    if (results.size() > 0) {
      return results.get(0); // These should be unique anyway...
    }
    return null;
  }
  
  public static List<Member> all() {
    return find.all();
  }
  
  public static void create(Member member) {
    member.save();
  }
  
  public static void delete(Long id) {
    find.ref(id).delete();
  }
    
}