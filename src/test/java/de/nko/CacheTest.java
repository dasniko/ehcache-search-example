package de.nko;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.search.expression.Criteria;

import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
public class CacheTest {
    private static Cache cache = CacheManager.getInstance().getCache("persons");
    private static Attribute<String> lastname = cache.getSearchAttribute("lastname");
    private static Attribute<String> gender = cache.getSearchAttribute("gender");
    private static Attribute<String> country = cache.getSearchAttribute("country");

    @BeforeClass
    public static void init() throws Exception {
        List<Person> persons = createPersons();
        populateCache(persons);
    }

    @Test
    public void searchLastnameSmith() throws Exception {
        log.info("Preparing search for lastname='Smith'...");
        int n = performSearch(lastname.eq("Smith"));
        assertEquals(3890, n);
    }

    @Test
    public void searchGenderM() throws Exception {
        log.info("Preparing search for gender='M'...");
        int n = performSearch(gender.eq("M"));
        assertEquals(499530, n);
    }

    @Test
    public void searchCountryGermany() throws Exception {
        log.info("Preparing search for country='Germany'...");
        int n = performSearch(country.eq("Germany"));
        assertEquals(4010, n);
    }

    @Test
    public void searchGenderFCountryFrance() throws Exception {
        log.info("Preparing search for gender='F' and country='France'...");
        int n = performSearch(gender.eq("F"), country.eq("France"));
        assertEquals(1940, n);
    }

    @Test
    public void searchGenderMLastnameSchmidt() throws Exception {
        log.info("Preparing search for gender='M' and lastname='Schmidt'...");
        int n = performSearch(gender.eq("M"), lastname.eq("Schmidt"));
        assertEquals(2240, n);
    }

    @Test
    public void searchLastnameJonesCountryUSA() throws Exception {
        log.info("Preparing search for lastname='Jones' and country = 'United States of America'");
        int n = performSearch(lastname.eq("Jones"), country.eq("United States of America"));
        assertEquals(30, n);
    }

    private int performSearch(Criteria... criteria) {
        Query query = createQuery(criteria);
        long start = System.currentTimeMillis();
        Results results = query.execute();
        int count = results.size();
        long duration = System.currentTimeMillis() - start;
        log.info("Searchresult: found {} persons in {} ms.", count, duration);
        results.discard();
        return count;
    }

    private Query createQuery(Criteria... criteria) {
        Query query = cache.createQuery().includeValues();
        for (Criteria crit : criteria) {
            query.addCriteria(crit);
        }
        query.end();
        return query;
    }

    private static void populateCache(List<Person> persons) {
        log.info("Put persons into cache...");
        int n = persons.size();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            for (Person p : persons) {
                int k = p.getId() + n * i;
                Element e = new Element(k, p);
                cache.put(e);
            }
        }
        long duration = System.currentTimeMillis() - start;
        log.info("All persons cached in {}ms.", duration);
        log.info("Cache size is {} elements", cache.getStatistics().getLocalHeapSize());
        // log.info("Cache size is {}MB", cache.getStatistics().getLocalHeapSizeInBytes() / 1024 / 1024);
    }

    private static List<Person> createPersons() throws Exception {
        log.info("Creating persons from repository...");
        List<Person> persons = new ArrayList<Person>();

        InputStream is = CacheTest.class.getResourceAsStream("/mock_persons.csv");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        while ((line = br.readLine()) != null) {
            String[] strings = line.split(",");
            Person p = new Person();
            p.setId(Integer.parseInt(strings[0]));
            p.setFirstname(strings[1]);
            p.setLastname(strings[2]);
            p.setGender(strings[3]);
            p.setCountry(strings[4]);
            persons.add(p);
        }

        br.close();
        is.close();

        log.info("Created {} persons from repository.", persons.size());
        return persons;
    }

}
