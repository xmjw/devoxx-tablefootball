package utils;

import java.util.*;
import models.*;

// Used to compare teams based on their computed score.
public class TeamComparator implements Comparator<Team> {
    @Override
    public int compare(Team left, Team right) {
      return right.computeScore() - left.computeScore();
    }
}