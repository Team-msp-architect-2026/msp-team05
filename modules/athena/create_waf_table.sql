CREATE EXTERNAL TABLE IF NOT EXISTS waf_logs (
  timestamp                    BIGINT,
  formatversion                INT,
  webaclid                     STRING,
  terminatingruleid            STRING,
  terminatingruletype          STRING,
  action                       STRING,
  terminatingrulematchdetails  ARRAY<STRING>,
  httpsourcename               STRING,
  httpsourceid                 STRING,
  rulegrouplist                ARRAY<STRING>,
  ratebasedrulelist            ARRAY<STRING>,
  nonterminatingmatchingrules  ARRAY<STRING>,
  requestheadersinserted       STRING,
  responsecodesent             STRING,
  httprequest STRUCT<
    clientip:    STRING,
    country:     STRING,
    headers:     ARRAY<STRUCT<name:STRING, value:STRING>>,
    uri:         STRING,
    args:        STRING,
    httpversion: STRING,
    httpmethod:  STRING,
    requestid:   STRING,
    fragment:    STRING,
    scheme:      STRING,
    host:        STRING
  >,
  labels ARRAY<STRUCT<name:STRING>>,
  ja3fingerprint STRING,
  ja4fingerprint STRING
)
ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
WITH SERDEPROPERTIES ('ignore.malformed.json' = 'true')
STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION 's3://aws-waf-logs-kky-prod/AWSLogs/611058323802/WAFLogs/cloudfront/kky-prod-waf/'
TBLPROPERTIES (
  'has_encrypted_data'='false',
  'compression_type'='gzip'
);
