

public enum UserType{

    PIPELINEOPERATOR("pipeline_operator"),
    PIPELINEENGINEER("pipeline_engineer")

    String description;

    // Constructor
    UserType(String description){
        this.description = description;
    }

    //Getters
    public String getDescription(){
        return description;
    }

}
