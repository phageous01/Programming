// Spreadsheet is a programming problem for designing spreadsheet like functionality.
// You are asked to implement two functions, set_cell and get_cell.
// set_cell updates a cell's value with either a numeric value or a formula. For
// simplicity sake, the code only supports + and -.
// get_cell returns the calculated value of a particular cell.
// If the formula in set_cell introduces any circular dependency, all cells will 
// be set to null.
public class Main {
    public static void main(String[] args) {
        SpreadSheet sp = new SpreadSheet();
        sp.set_cell("A1", "13");
        sp.set_cell("A2", "14");
        // single value
        System.out.println(sp.get_cell("A1")); //13
        
        // formula
        sp.set_cell("A3", "=A1+A2");
        System.out.println(sp.get_cell("A3")); //27
        
        // multi-level formula
        sp.set_cell("A4", "=A3+11-6+A3");
        System.out.println(sp.get_cell("A4")); //59
        
        // change of val is propagated
        sp.set_cell("A1", "23");
        System.out.println(sp.get_cell("A3")); //37
        System.out.println(sp.get_cell("A4")); //79
        
        // circular
        sp.set_cell("A2", "=A4+12");
        System.out.println(sp.get_cell("A2")); // null
        System.out.println(sp.get_cell("A3")); // null
        System.out.println(sp.get_cell("A4")); // null
        
        // remove circular
        sp.set_cell("A2", "14");
        System.out.println(sp.get_cell("A2")); //14
        System.out.println(sp.get_cell("A3")); //37
        System.out.println(sp.get_cell("A4")); //79
        
        // Update formula to change dependency
        sp.set_cell("A3", "=4+A2");
        System.out.println(sp.get_cell("A3")); //18
        sp.set_cell("A1", "15");
        // A3 no longer depends on A1, so change of A1 will not change A3
        System.out.println(sp.get_cell("A3")); //18
        
    }
    
    public static class SpreadSheet{
        Map<String, String> formulaMap = new HashMap<>();
        Map<String, Integer> valueMap = new HashMap<>();
        Map<String, Set<String>> dependencyMap = new HashMap<>();
        
        public void set_cell(String key, String val){
            if (formulaMap.containsKey(key)) {
                for (String d : getDependencies(formulaMap.get(key))) {
                    if (d.isEmpty()) {
                        continue;
                    }
                    dependencyMap.get(d).remove(key);
                }
            }
            Integer intVal = toInt(val);
            boolean hasCycle = false;
            if (intVal != null) {
                // numeric value
                formulaMap.remove(key);
                valueMap.put(key, intVal);
            } else {
                // formula value
                formulaMap.put(key, val);
                
                // Analyze cycles.
                Set<String> circularCells = new HashSet<>();
                hasCycle = hasCycle(key, circularCells);
                if (hasCycle) {
                    // Update all circular cells values to null
                    for (String c : circularCells) {
                        valueMap.put(c, null);
                    }
                } else {
                    // calculate the value using valueMap
                    valueMap.put(key, calculate(val));
                }
                
                // Update dependencies
                for (String d : getDependencies(val)) {
                    dependencyMap.putIfAbsent(d, new HashSet<>());
                    dependencyMap.get(d).add(key);
                }
            }
            // Propagate the changes if no cycles
            if (!hasCycle) {
                propagate(key);
            }
        }

        public Integer get_cell(String key){
            return valueMap.getOrDefault(key, null);
        }
        
        private String[] getDependencies(String formula) {
            return formula.split("[-+=]");
        }
        
        private Integer toInt(String val) {
            try {
                return Integer.valueOf(val);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        private boolean hasCycle(String key, Set<String> visited) {
            visited.add(key);
            if (formulaMap.containsKey(key)) {
                for (String d : getDependencies(formulaMap.get(key))) {
                    if (d.isEmpty()) {
                        continue;
                    }
                    if (visited.contains(d)) {
                        System.out.printf(
                            "Cycle detected: %s, visited %s\n", d, visited.toString());
                        return true;
                    }
                    if (hasCycle(d, visited)) {
                        return true;
                    }
                }
            }
            
            visited.remove(key);
            return false;
        }
        
        private int calculate(String formula) {
            int result = 0;
            int index = 1; // formula starts with =
            int factor = 1;
            while (index < formula.length()) {
                int tokenEnd = index+1;
                while (tokenEnd < formula.length() && formula.charAt(tokenEnd) != '+' 
                      && formula.charAt(tokenEnd) != '-') {
                    tokenEnd++;
                }
                String token = formula.substring(index, tokenEnd);
                Integer tokenVal = toInt(token);
                if (tokenVal != null) {
                    result += tokenVal * factor;
                } else {
                    result += valueMap.get(token)*factor;
                }
                if (tokenEnd != formula.length()) {
                    if (formula.charAt(tokenEnd) == '+') {
                        factor = 1;
                    } else {
                        factor = -1;
                    }
                }
                index = tokenEnd+1;
            }
            return result;
        }
        
        private void propagate(String key) {
            if (dependencyMap.containsKey(key)) {
                for (String d : dependencyMap.get(key)) {
                    valueMap.put(d, calculate(formulaMap.get(d)));
                    propagate(d);
                }
            }
        }
    }
}
