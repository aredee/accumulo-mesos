package aredee.mesos.frameworks.accumulo.model;

import java.util.ArrayList;

public class Monitor extends ArrayList<String> {

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class Monitor {\n");
        sb.append("  " + super.toString()).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
