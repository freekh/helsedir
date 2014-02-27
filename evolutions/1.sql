-- UPS
ALTER TABLE "file_2011_12_aktivitet_HF_NASJ_txt" ALTER COLUMN "hf" RENAME TO "HF";
ALTER TABLE "file_2012_aktivitet_HF_NASJ_txt" ALTER COLUMN "hf" RENAME TO "HF";


ALTER TABLE "file_2011_12_aktivitet_HF_NASJ_txt" ADD COLUMN IF NOT EXISTS "drg_type" VARCHAR AFTER "KMF";

ALTER TABLE "file_2011_12_aktivitet_HF_NASJ_txt" ADD COLUMN IF NOT EXISTS "liggetid_grp" INTEGER AFTER "drg_type";
ALTER TABLE "file_2011_12_aktivitet_HF_NASJ_txt" ADD COLUMN IF NOT EXISTS "pasientgruppe" INTEGER AFTER "liggetid_grp";

ALTER TABLE "file_2012_aktivitet_HF_NASJ_txt" ADD COLUMN IF NOT EXISTS "liggetid_grp" INTEGER AFTER "drg_type";
ALTER TABLE "file_2012_aktivitet_HF_NASJ_txt" ADD COLUMN IF NOT EXISTS "pasientgruppe" INTEGER AFTER "liggetid_grp";

CREATE TABLE aktivitet AS SELECT * FROM "file_2011_12_aktivitet_HF_NASJ_txt" UNION ALL SELECT * FROM "file_2012_aktivitet_HF_NASJ_txt" UNION ALL SELECT * FROM "file_2013_12_aktivitet_HF_NASJ_txt";
