#!/bin/sh
set -eu

raw_url="${SPRING_DATASOURCE_URL:-${DATABASE_URL:-}}"

if [ -n "$raw_url" ]; then
  case "$raw_url" in
    jdbc:*)
      export SPRING_DATASOURCE_URL="$raw_url"
      ;;
    postgresql://*|postgres://*)
      url_no_scheme="${raw_url#postgresql://}"
      url_no_scheme="${url_no_scheme#postgres://}"

      authority="${url_no_scheme%%/*}"
      path_and_query="${url_no_scheme#*/}"

      if [ "$authority" = "${authority#*@}" ]; then
        export SPRING_DATASOURCE_URL="jdbc:postgresql://$authority/$path_and_query"
      else
        creds="${authority%@*}"
        hostpart="${authority#*@}"
        db_url="jdbc:postgresql://$hostpart/$path_and_query"

        if [ -n "$creds" ]; then
          export SPRING_DATASOURCE_URL="$db_url"
          export SPRING_DATASOURCE_USERNAME="${creds%%:*}"
          if [ "$creds" != "${creds#*:}" ]; then
            export SPRING_DATASOURCE_PASSWORD="${creds#*:}"
          fi
        else
          export SPRING_DATASOURCE_URL="$db_url"
        fi
      fi
      ;;
    *)
      export SPRING_DATASOURCE_URL="jdbc:$raw_url"
      ;;
  esac
fi

exec java -Dserver.port="${PORT:-8080}" ${JAVA_TOOL_OPTIONS:-} -jar /app/app.jar
