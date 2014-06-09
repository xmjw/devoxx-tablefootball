package utils;

import java.util.*;
import models.*;

// Used to compare teams based on their computed score.
public class TeamComparator implements Comparator<Team> {
    @Override
    public int compare(Team left, Team right) {
      int res = right.wins - left.wins;
      if (res == 0) res = left.loses - right.loses;
      return res;
    }
}