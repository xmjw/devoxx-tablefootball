package utils;

public class State {
  public String state;
  public int data;
  public boolean has_data;
  public String from;

  public boolean is(String condition) {
    return state.equals(condition);
  }
  
  public State(String state, String from, int data) {
    this.data = data;
    this.state = state;
    this.from = from;
    has_data = true;
  }
  
  public State(String state, String from) {
    this.state = state;
    this.from = from;
    this.data = -1;
    has_data = false;
  }  
}