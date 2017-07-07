package org.openlca.updates.script;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;

public class CalculationContext {

	public final MatrixCache matrixCache;
	public final EntityCache entityCache;
	public final IMatrixSolver solver;

	public CalculationContext(MatrixCache matrixCache, EntityCache entityCache, IMatrixSolver solver) {
		this.matrixCache = matrixCache;
		this.entityCache = entityCache;
		this.solver = solver;
	}

}
