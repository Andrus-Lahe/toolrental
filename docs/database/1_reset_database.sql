-- Kustutab tool_rental schema (mis põhimõtteliselt kustutab kõik tabelid)
DROP SCHEMA IF EXISTS tool_rental CASCADE;
-- Loob uue tool_rental schema vajalikud õigused
CREATE SCHEMA tool_rental
-- taastab vajalikud andmebaasi õigused
    GRANT ALL ON SCHEMA tool_rental TO postgres;
GRANT ALL ON SCHEMA tool_rental TO PUBLIC;