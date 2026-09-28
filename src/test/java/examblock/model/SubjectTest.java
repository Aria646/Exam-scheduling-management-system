package examblock.model;

import examblock.view.components.Verbose;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * [6] implement SubjectTest in JUnit
 */
public class SubjectTest {

    private Registry registry;
    private Subject subject;

    @Before
    public void setUp() {
        registry = new Registry();
        subject = new Subject("  General   Mathematics  ", "  study of maths  ", registry);
    }

    // ==================== SANITISE TITLE ====================

    @Test
    public void testSanitiseTitle_null() {
        assertEquals("", subject.sanitiseTitle(null));
    }

    @Test
    public void testSanitiseTitle_leadingTrailingSpaces() {
        assertEquals("Mathematics", subject.sanitiseTitle("  Mathematics  "));
    }

    @Test
    public void testSanitiseTitle_multipleSpaces() {
        assertEquals("General Mathematics", subject.sanitiseTitle("General   Mathematics"));
    }

    @Test
    public void testSanitiseTitle_trailingFullStop() {
        assertEquals("General Mathematics", subject.sanitiseTitle("General Mathematics."));
    }

    // ==================== SANITISE DESCRIPTION ====================

    @Test
    public void testSanitiseDescription_null() {
        assertEquals("", subject.sanitiseDescription(null));
    }

    @Test
    public void testSanitiseDescription_empty() {
        assertEquals(".", subject.sanitiseDescription("   "));
    }

    @Test
    public void testSanitiseDescription_lowercaseFirstLetter() {
        String result = subject.sanitiseDescription("study of mathematics");
        assertEquals("Study of mathematics.", result);
    }

    @Test
    public void testSanitiseDescription_noTrailingStop() {
        String result = subject.sanitiseDescription("Study of mathematics");
        assertEquals("Study of mathematics.", result);
    }

    @Test
    public void testSanitiseDescription_alreadyCapitalAndStop() {
        String input = "Study of mathematics.";
        String result = subject.sanitiseDescription(input);
        assertEquals("Study of mathematics.", result);
    }

    @Test
    public void testSanitiseDescription_multipleSentences() {
        String input = "first sentence. second sentence.";
        String result = subject.sanitiseDescription(input);
        assertEquals("First sentence. second sentence.", result);
    }

    // ==================== CONSTRUCTOR ====================

    @Test
    public void testConstructor_sanitisesTitleAndDescription() {
        Subject s = new Subject("  Biology  101  ", "  biology is fun  ", registry);
        assertEquals("Biology 101", s.getTitle());
        assertEquals("Biology is fun.", s.getDescription());
    }

    @Test
    public void testConstructor_registersWithRegistry() {
        String id = "General Mathematics";
        assertTrue(registry.contains(id, Subject.class));
        Subject retrieved = registry.get(id, Subject.class);
        assertEquals(subject, retrieved);
    }

    // ==================== GETTERS ====================

    @Test
    public void testGetTitle() {
        assertEquals("General Mathematics", subject.getTitle());
    }

    @Test
    public void testGetDescription() {
        assertEquals("Study of maths.", subject.getDescription());
    }

    // ==================== ID ====================

    @Test
    public void testGetId() {
        assertEquals("General Mathematics", subject.getId());
    }

    // ==================== TO TABLE ROW ====================

    @Test
    public void testToTableRow() {
        Object[] row = subject.toTableRow();
        assertEquals(1, row.length);
        assertEquals("General Mathematics", row[0]);
    }

    // ==================== FULL DETAIL & TO STRING ====================

    @Test
    public void testGetFullDetail() {
        String detail = subject.getFullDetail();
        assertTrue(detail.contains("GENERAL MATHEMATICS"));
        assertTrue(detail.contains("\"Study of maths.\""));
    }

    @Test
    public void testToString() {
        assertEquals("GENERAL MATHEMATICS", subject.toString());
    }

    // ==================== EQUALS & HASHCODE ====================

    @Test
    public void testEquals_sameObject() {
        assertEquals(subject, subject);
    }

    @Test
    public void testEquals_differentTitle() {
        Subject other = new Subject("Physics", "Study of physics.", registry);
        assertNotEquals(subject, other);
    }

    @Test
    public void testEquals_differentDescription() {
        Registry tempReg = new Registry();
        Subject other = new Subject("General Mathematics", "Different description.", tempReg);
        assertNotEquals(subject, other);
    }

    @Test
    public void testEquals_null() {
        assertNotEquals(null, subject);
    }

    @Test
    public void testEquals_differentClass() {
        assertNotEquals(subject, "string");
    }

    @Test
    public void testHashCode_consistent() {
        Registry tempReg = new Registry();
        Subject same = new Subject("General Mathematics", "Study of maths.", tempReg);
        assertEquals(subject, same);
    }

    // ==================== STREAMING ====================

    @Test
    public void testStreamOut_writesCorrectFormat() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        subject.streamOut(bw, 5);
        bw.flush();
        String output = sw.toString();

        String[] lines = output.split("\n");
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("5. GENERAL MATHEMATICS"));
        assertEquals("General Mathematics", lines[1]);
        assertEquals("\"Study of maths.\"", lines[2]);
    }

    @Test
    public void testStreamIn_normal() throws IOException {
        Registry tempReg = new Registry();
        String input = "3. GENERAL MATHEMATICS\nGeneral Mathematics\n\"Study of maths.\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        Verbose.setVerbose(false);
        Subject loaded = new Subject(br, tempReg, 3);

        assertEquals("General Mathematics", loaded.getTitle());
        assertEquals("Study of maths.", loaded.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamIn_indexMismatch() throws IOException {
        String input = "5. GENERAL MATHEMATICS\nGeneral Mathematics\n\"Study of maths.\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        Verbose.setVerbose(false);
        new Subject(br, registry, 3); // index mismatch
    }

    @Test(expected = RuntimeException.class)
    public void testStreamIn_eofDuringHeading() throws IOException {
        String input = "";
        BufferedReader br = new BufferedReader(new StringReader(input));
        Verbose.setVerbose(false);
        new Subject(br, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamIn_missingTitleLine() throws IOException {
        String input = "1. GENERAL MATHEMATICS\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        Verbose.setVerbose(false);
        new Subject(br, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamIn_missingDescriptionLine() throws IOException {
        String input = "1. GENERAL MATHEMATICS\nGeneral Mathematics\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        Verbose.setVerbose(false);
        new Subject(br, registry, 1);
    }

    // ==================== VERBOSE MODE (optional coverage) ====================

    @Test
    public void testStreamIn_verboseOff_doesNotPrint() throws IOException {
        String input = "2. MATHS\nMathematics\n\"Study.\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        // Capture System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        Verbose.setVerbose(false);
        new Subject(br, registry, 2);

        System.setOut(originalOut);
        assertFalse(baos.toString().contains("Loaded Subject:"));
    }

    @Test
    public void testStreamIn_verboseOn_printsMessage() throws IOException {
        String input = "3. ENGLISH\nEnglish\n\"Study.\"\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        Verbose.setVerbose(true);
        new Subject(br, registry, 3);

        System.setOut(originalOut);
        Verbose.setVerbose(false);
        assertTrue(baos.toString().contains("Loaded Subject: English"));
    }
}