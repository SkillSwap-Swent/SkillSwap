package com.swent.skillswap.model.tags

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

    override fun toString(): String = label
}
