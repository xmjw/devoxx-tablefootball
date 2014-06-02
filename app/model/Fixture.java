package models;

import java.util.*;

import play.db.ebean.*;
import play.data.validation.Constraints.*;

import javax.persistence.*;

@Entity
public class Fixture extends Model {

  @Id
  public Long id;
    
  public static Finder<Long,Fixture> find = new Finder(
    Long.class, Fixture.class
  );
  
  public static List<Fixture> all() {
    return find.all();
  }
  
  public static void create(Fixture fixture) {
    fixture.save();
  }
  
  public static void delete(Long id) {
    find.ref(id).delete();
  }
    
}