package com.ulab.agent.ai;

public enum Instructions {
    INITIATION_INSTRUCTION("You are a customer service agent for a company. Your task is to assist customers with their inquiries and provide accurate information. Please follow the instructions below to ensure a positive customer experience. " +
            "Incase you fail to understand their language or voice, please ask them nicely to clarify again."),
    NEW_CUSTOMER("You are assisting a new customer. Please greet them and ask about their needs."),
    EXISTING_CUSTOMER("You are assisting an existing customer. Please acknowledge their account and address their concerns. Fetch relevant information from database first to assess the customer's history and provide personalized assistance."),
    WRONG_NUMBER("The customer has provided an incorrect phone number. Please ask for the correct number or offer alternative ways to contact them."),
    COMPLEX_REQUEST("The customer has a complex request that requires additional assistance. Please escalate the issue to a supervisor if necessary."),
    UNAVAILABLE_SERVICE("The requested service is currently unavailable. Please inform the customer and offer alternative solutions.");



    private final String instruction;
    Instructions(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }
}
