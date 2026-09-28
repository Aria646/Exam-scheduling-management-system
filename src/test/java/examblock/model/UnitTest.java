package examblock.model;

import examblock.view.components.Verbose;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
/**
 * [6] implement UnitTest in JUnit
 */
public class UnitTest {

    private Registry registry;
    private Subject subject;
    private Unit unit;

    @Before
    public void setUp() {
        registry = new Registry();
        subject = new Subject("Ancient History", "Study of ancient civilisations.", registry);
        unit = new Unit(subject, '3', "Reconstructing the Ancient World",
                "Investigate significant historical periods through an analysis of relevant archaeological and written sources.",
                registry);
    }

    // === Sanitisation (private methods indirectly tested via constructors) ===
    @Test
    public void testConstructor_sanitisesTitle() {
        Registry tempRegistry=new Registry();
        Unit u = new Unit(subject, '3', "  Reconstructing   the   Ancient World  .   ",
                "Valid description.", tempRegistry);
        assertEquals("Reconstructing the Ancient World", u.getFullDetail().split("\n")[0].split(": ")[1].trim());
    }

    @Test
    public void testConstructor_sanitisesDescription() {
        Registry tempRegistry=new Registry();
        Unit u = new Unit(subject, '3', "Some title",
                "  lower case start  ", tempRegistry);
        String desc = u.getFullDetail().split("\n")[1];
        assertTrue(desc.startsWith("\"Lower case start.\""));
    }

    // === Getters ===
    @Test
    public void testGetSubject() {
        assertEquals(subject, unit.getSubject());
    }

    @Test
    public void testId() {
        assertEquals(Character.valueOf('3'), unit.id());
    }

    @Test
    public void testGetDescription() {
        assertTrue(unit.getDescription().startsWith("Investigate"));
    }

    // === FullDetail & toString ===
    @Test
    public void testGetFullDetail() {
        String detail = unit.getFullDetail();
        assertTrue(detail.contains("Ancient History, Unit 3: Reconstructing the Ancient World"));
        assertTrue(detail.contains("\"Investigate"));
    }

    @Test
    public void testToString() {
        assertEquals("Unit 3: Reconstructing the Ancient World", unit.toString());
    }

    // === toTableRow ===
    @Test
    public void testToTableRow() {
        Object[] row = unit.toTableRow();
        assertEquals(4, row.length);
        assertEquals("Ancient History", row[0]);
        assertEquals('3', row[1]);
        assertEquals("Reconstructing the Ancient World", row[2]);
        assertTrue(((String) row[3]).startsWith("Investigate"));
    }

    // === getId ===
    @Test
    public void testGetId() {
        assertEquals("Ancient History-3", unit.getId());
    }

    // === equals & hashCode ===
    @Test
    public void testEquals_same() {
        Registry tempRegistry=new Registry();
        Unit same = new Unit(subject, '3', "Reconstructing the Ancient World",
                "Investigate significant historical periods through an analysis of relevant archaeological and written sources.",
                tempRegistry);
        assertEquals(unit, same);
    }

    @Test
    public void testEquals_differentUnitId() {
        Unit other = new Unit(subject, '4', "Reconstructing the Ancient World", "Same", registry);
        assertNotEquals(unit, other);
    }

    @Test
    public void testEquals_differentTitle() {
        Registry tempRegistry=new Registry();
        Unit other = new Unit(subject, '3', "Different Title", unit.getDescription(), tempRegistry);
        assertNotEquals(unit, other);
    }

    @Test
    public void testEquals_differentSubject() {
        Subject another = new Subject("Maths", "Study", registry);
        Unit other = new Unit(another, '3', unit.getFullDetail().split("\n")[0].split(": ")[1], unit.getDescription(), registry);
        assertNotEquals(unit, other);
    }

    @Test
    public void testHashCode_consistent() {
        Registry tempRegistry=new Registry();
        Unit same = new Unit(subject, '3', unit.toString().split(": ")[1], unit.getDescription(), tempRegistry);
        assertEquals(unit.hashCode(), same.hashCode());
    }

    // === Streaming ===
    @Test
    public void testStreamOut_format() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        unit.streamOut(bw, 3);
        bw.flush();
        String[] lines = sw.toString().split("\n");
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("3. ANCIENT HISTORY"));
        assertTrue(lines[1].startsWith("Ancient History, Unit 3: Reconstructing the Ancient World"));
        assertTrue(lines[2].startsWith("\""));
    }

    @Test
    public void testStreamIn_normal() throws IOException {
        Registry tempRegistry=new Registry();
        tempRegistry.add(subject, Subject.class);
        String input = "3. ANCIENT HISTORY\n" +
                "Ancient History, Unit 3: Reconstructing the Ancient World\n" +
                "\"Investigate significant historical periods through an analysis of relevant archaeological and written sources.\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        Verbose.setVerbose(false);
        Unit loaded = new Unit(br, tempRegistry, 3);
        assertEquals(unit, loaded);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamIn_indexMismatch() throws IOException {
        String input = "5. ANCIENT HISTORY\nAncient History, Unit 3: Title\n\"Desc.\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Unit(br, registry, 3);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamIn_missingComma() throws IOException {
        String input = "1. MATHS\nMaths Unit 3: Title\n\"Desc\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Unit(br, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamIn_noQuotes() throws IOException {
        String input = "1. MATHS\nMaths, Unit 3: Title\nNo quotes\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        new Unit(br, registry, 1);
    }
}