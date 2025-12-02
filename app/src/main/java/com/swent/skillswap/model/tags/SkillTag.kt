/** Comments with ChatGPT */
package com.swent.skillswap.model.tags

/**
 * Represents the predefined set of skill categories available in the SkillSwap platform. Each tag
 * corresponds to a specific academic, technical, or engineering-related discipline.
 *
 * @property label A human-readable display name for UI purposes. This is intended for presentation
 *   only; use [name] for serialization.
 *
 * Usage notes:
 * - `name` provides the stable enum identifier (e.g., "DATA_STRUCTURES") for serialization,
 *   persistence, or API communication.
 * - `label` provides a clean, user-facing title (e.g., "Data Structures") suitable for UI.
 * - [toUIString] is a convenience method returning the display label.
 */
enum class SkillTag(val label: String) : EveryTag {
    CALCULUS("Calculus"),
    LINEAR_ALGEBRA("Linear Algebra"),
    DIFFERENTIAL_EQUATIONS("Differential Equations"),
    PHYSICS_MECHANICS("Physics: Mechanics"),
    PHYSICS_ELECTROMAGNETISM("Physics: Electromagnetism"),
    CHEMISTRY("Chemistry"),
    COMPUTER_PROGRAMMING("Computer Programming"),
    DATA_STRUCTURES("Data Structures"),
    ALGORITHMS("Algorithms"),
    DATABASES("Databases"),
    OPERATING_SYSTEMS("Operating Systems"),
    CIRCUIT_ANALYSIS("Circuit Analysis"),
    DIGITAL_LOGIC("Digital Logic"),
    MICROCONTROLLERS("Microcontrollers"),
    FLUID_MECHANICS("Fluid Mechanics"),
    THERMODYNAMICS("Thermodynamics"),
    MATERIALS_ENGINEERING("Materials Engineering"),
    STRUCTURAL_ANALYSIS("Structural Analysis"),
    TRANSPORT_PHENOMENA("Transport Phenomena"),
    CONTROL_SYSTEMS("Control Systems"),
    SIGNAL_PROCESSING("Signal Processing"),
    MACHINE_DESIGN("Machine Design"),
    PROJECT_MANAGEMENT("Project Management"),
    ENGINEERING_ETHICS("Engineering Ethics");

    /**
     * Returns the user-friendly label for display purposes. Equivalent to accessing [label]
     * directly.
     */
    fun toUIString(): String = label
}
