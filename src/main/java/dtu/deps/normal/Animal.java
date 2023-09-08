package dtu.deps.normal;

abstract class Animal {
    protected String name;

    public void eat() {
        System.out.println(name + " is eating.");
    }

    public void sleep() {
        System.out.println(name + " is sleeping.");
    }
}

class Dog extends Animal {
    private String breed;

    public void bark() {
        System.out.println(name + " is barking.");
    }
}