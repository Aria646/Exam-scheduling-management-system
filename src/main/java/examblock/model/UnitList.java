package examblock.model;

/**
 * A collection object for holding and managing {@link Unit}s.
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class UnitList extends ListManager<Unit> {

    /**
     * constructor
     *
     * @param registry registry
     */
    public UnitList(Registry registry) {
        super(Unit::new, registry, Unit.class);
    }

    /**
     * Get the first {@link Unit} with a matching {@link Subject} and {@code unitId}.
     *
     * @param subjectTitle - the {@code title} of the parent {@link Subject} of the
     *                     {@link Unit} to be found.
     * @param unitId       - the unit identifier of the {@link Subject} {@link Unit} to be found.
     * @return first {@link Unit} with a matching subject {@code title} and {@code unitId},
     * if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *                               find a matching unit as that indicates there is a misalignment
     *                               of the executing state and the complete list of possible units.
     */
    public Unit getUnit(String subjectTitle, Character unitId) throws IllegalStateException {
        for (Unit unit : all()) {
            if (unit.getSubject().getTitle().equals(subjectTitle) && unit.id().equals(unitId)) {
                return unit;
            }
        }
        throw new IllegalStateException("No unit found for " + subjectTitle + " unit " + unitId);
    }

    /**
     * Returns detailed string representations of the contents of this unit list.
     *
     * @return detailed string representations of the contents of this unit list.
     */
    public String getFullDetail() {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (Unit unit : all()) {
            sb.append(counter).append(". ").append(unit.getFullDetail()).append("\n");
            counter++;
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of the contents of the unit list
     *
     * @return a string representation of the contents of the unit list
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (Unit unit : all()) {
            sb.append(counter).append(". ").append(unit.toString()).append("\n");
            counter++;
        }
        return sb.toString();
    }
}