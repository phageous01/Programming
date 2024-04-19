/*
 * I recently did a coding interview with one of the most prestigious tech companies out there. I was asked to implement an in memory database
 * that supports SQL like functionality: create table, query by primary key, query by column, support AND / OR, and order by. It is not that
 * hard, but you do need to write a lot of code in a stressful environment. Testing in Java also sucks big time. At any rate, I decided to
 * redo this problem offline and share it on github.
 *
 */
public class Main {
    public static void main(String[] args) {
        
        // Create DB.
        InMemoryDb db = new InMemoryDb("testdb");
        
        // Add a table.
        InMemoryDbTable table = new InMemoryDbTable("table1", Arrays.asList("col1", "col2", "col3"));
        db.addTable(table);
        
        // Add a few rows.
        table.addRow("row1-col1", new HashMap<>() {{
            put("col1", "row1-col1");
            put("col2", "row1-col2");
            put("col3", "row1-col3");
        }});
        table.addRow("row2-col1", new HashMap<>() {{
            put("col1", "row2-col1");
            put("col2", "row2-col2");
            put("col3", "row2-col3");
        }});
        table.addRow("row3-col1", new HashMap<>() {{
            put("col1", "row3-col1");
            put("col2", "row3-col2");
            put("col3", "row3-col3");
        }});        
        
        // Get row by primary key.
        System.out.printf("Row: %s\n", table.getValueWithKey("row1-col1").rowData.toString());
        
        // Get row by column query and AND operator
        System.out.printf("Row: %s\n", table.getValuesWithAnd(new HashMap<>() {{
            put("col2", "row2-col2");
            put("col3", "row2-col3");
        }}).get(0).rowData.toString());
        
        // Get row by column query and OR operator
        Map<String, String> query = new HashMap<>() {{
            put("col2", "row2-col2");
            put("col3", "row3-col3");
        }};
        System.out.printf("Row: %s\n", table.getValuesWithOr(query).get(0).rowData.toString());
        System.out.printf("Row: %s\n", table.getValuesWithOr(query).get(1).rowData.toString());
        
        // Order by
        System.out.printf("Row: %s\n", table.getValuesWithOrOrderBy(query, "col2").get(0).rowData.toString());
        System.out.printf("Row: %s\n", table.getValuesWithOrOrderBy(query, "col2").get(1).rowData.toString());
    }
    
    static class InMemoryDb {
        String name;
        Map<String, InMemoryDbTable> tables;
        
        InMemoryDb(String name) {
            this.name = name;
            this.tables = new HashMap<>();
        }
        
        void addTable(InMemoryDbTable table) {
            tables.put(table.name, table);
        }
    }
    
    static class InMemoryDbTable {
        String name;
        List<String> columns;
        Map<String, InMemoryDbRow> rows;
        
        InMemoryDbTable(String name, List<String> columns) {
            this.name = name;
            this.columns = columns;
            this.rows = new HashMap<>();
        }
        
        public boolean addRow(String key, Map<String, String> rowData) {
            if (rows.containsKey(key)) {
                return false;
            }
            rows.put(key, new InMemoryDbRow(rowData));
            return true;
        }
        
        public InMemoryDbRow getValueWithKey(String key) {
            return rows.getOrDefault(key, null);
        }
        
        public List<InMemoryDbRow> getValuesWithAnd(Map<String, String> query) {
            List<InMemoryDbRow> result = new ArrayList<>();
            for (InMemoryDbRow row : rows.values()) {
                boolean match = true;
                for (String k : query.keySet()) {
                    if (row.rowData.getOrDefault(k, null) != query.get(k)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    result.add(row);
                }
            }
            return result;
        }
        
        public List<InMemoryDbRow> getValuesWithOr(Map<String, String> query) {
            List<InMemoryDbRow> result = new ArrayList<>();
            for (InMemoryDbRow row : rows.values()) {
                for (String k : query.keySet()) {
                    if (row.rowData.getOrDefault(k, null) == query.get(k)) {
                        result.add(row);
                        break;
                    }
                }
            }
            return result;
        }
        
        public List<InMemoryDbRow> getValuesWithAndOrderBy(Map<String, String> query, String columnName) {
            List<InMemoryDbRow> result = getValuesWithAnd(query);
            result.sort((r1, r2)->r1.rowData.get(columnName).compareTo(r2.rowData.get(columnName)));
            return result;
        }
        
        public List<InMemoryDbRow> getValuesWithOrOrderBy(Map<String, String> query, String columnName) {
            List<InMemoryDbRow> result = getValuesWithOr(query);
            result.sort((r1, r2)->r1.rowData.get(columnName).compareTo(r2.rowData.get(columnName)));
            return result;
        }
    }
    
    static class InMemoryDbRow {
        Map<String, String> rowData;
        
        InMemoryDbRow(Map<String, String> rowData) {
            this.rowData = rowData;
        }
    }
}

