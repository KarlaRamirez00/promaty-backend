package com.promaty.rrhh.services.rexplus;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Termination;

public interface RexPlusClient {

	RexSyncResult syncContract(Contract contract);

	RexSyncResult annulContract(Contract contract, String reason);

	RexSyncResult syncTermination(Termination termination);

	RexSyncResult annulTermination(Termination termination, String reason);

	RexSyncResult syncAnnex(Annex annex);
}
