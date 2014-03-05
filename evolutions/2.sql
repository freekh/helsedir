-- UPS
ALTER TABLE AKTIVITET RENAME TO AKTIVITET_HF;

CREATE VIEW AKTIVITET_HF_CLEAN  AS
SELECT "AKTIVITET_HF"."utmnd","AKTIVITET_HF"."sykehusområde","Ref Pasientregion".PASIENTREGION, "AKTIVITET_HF"."opptaksområde","AKTIVITET_HF"."sh_reg","AKTIVITET_HF"."HF","AKTIVITET_HF"."ald_gr2","AKTIVITET_HF"."omsorgsniva","AKTIVITET_HF"."innmateHast","AKTIVITET_HF"."fraSted_kort","AKTIVITET_HF"."tilSted_kort","AKTIVITET_HF"."hdg","AKTIVITET_HF"."drg_omk","AKTIVITET_HF"."utskrivingsklar","AKTIVITET_HF"."KMF","AKTIVITET_HF"."drg_type","AKTIVITET_HF"."liggetid_grp","AKTIVITET_HF"."pasientgruppe","AKTIVITET_HF"."korrvekt_SUM","AKTIVITET_HF"."korrvekt_kmf_SUM","AKTIVITET_HF"."liggetid_sum","AKTIVITET_HF"."utskrKlarTid_sum","AKTIVITET_HF"."utskrKlarOpphold_sum","AKTIVITET_HF"."ant_opph" FROM "AKTIVITET_HF", "Ref Pasientregion"  WHERE "Ref Pasientregion"."PASIENTREGION_ID" = "AKTIVITET_HF"."pas_reg"
