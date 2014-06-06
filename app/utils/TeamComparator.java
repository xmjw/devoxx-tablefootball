package utils;

import java.util.*;
import models.*;

public class TeamComparator implements Comparator<Team> {
    @Override
    public int compare(Team left, Team right) {
      System.out.println("Comparing "+left.name+" and "+right.name+" for some reason.");
      return right.computeScore() - left.computeScore();
    }
}